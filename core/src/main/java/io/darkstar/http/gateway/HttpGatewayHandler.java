package io.darkstar.http.gateway;

import io.darkstar.http.Headers;
import io.darkstar.http.HttpObject;
import io.darkstar.http.MutableHeaders;
import io.darkstar.http.NettyHttpErrorResponder;
import io.darkstar.http.NettyHttpObjectSource;
import io.darkstar.http.Request;
import io.darkstar.net.DefaultHost;
import io.darkstar.net.Host;
import io.darkstar.net.HostParser;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static io.darkstar.http.HeaderName.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class HttpGatewayHandler extends ChannelHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(HttpGatewayHandler.class);

    private final NettyHttpErrorResponder ERROR_RESPONDER = new NettyHttpErrorResponder();

    @Autowired
    private OriginHostResolver originHostResolver;

    private Channel originServerChannel;

    //whether or not a frontend-to-backend tunnel is established
    private volatile boolean tunnelEstablished;

    private Host originServerHost; //Actual host that we connect to based on load balancing rules

    //queue inbound request fragments until a connection with the backend (a tunnel) is established
    //Once established, we relay the queued messages
    private Queue<HttpObject> messageQueue;

    public HttpGatewayHandler() {
        this.messageQueue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //start reading data immediately: we need to be able to read the inbound request headers to determine
        //which backend origin server to connect to:
        ctx.channel().read();
    }

    private void connectToDestination(final ChannelHandlerContext ctx, Host destination) throws Exception {

        final Channel gatewayChannel = ctx.channel();

        // Start the connection attempt.
        Bootstrap b = new Bootstrap()
            //Use the gatewayChannel's eventLoop:
            // - http://normanmaurer.me/presentations/2014-facebook-eng-netty/slides.html#27.0 :
            .group(gatewayChannel.eventLoop())
            .channel(ctx.channel().getClass())
            .handler(new HttpOriginChannelInitializer(gatewayChannel))
                // ALLOCATOR, see:
                // - https://blog.twitter.com/2013/netty-4-at-twitter-reduced-gc-overhead
                // - https://www.youtube.com/watch?v=_GRIyCMNGGI&t=1183
                // - http://normanmaurer.me/presentations/2014-facebook-eng-netty/slides.html#14.0
            .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
            .option(ChannelOption.AUTO_READ, false); //we will enable reading once the tunnel has been established

        ChannelFuture f = b.connect(destination.getName(), destination.getPort());

        this.originServerChannel = f.channel();

        f.addListener(future -> {
            if (future.isSuccess()) {
                tunnelEstablished(ctx);
            } else {
                //can't connect to backend - show error:
                ERROR_RESPONDER.respondWithError(HttpResponseStatus.SERVICE_UNAVAILABLE, ctx);
            }
        });
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {

        HttpObject httpObject = (HttpObject) msg;
        messageQueue.add(httpObject);

        if (tunnelEstablished) {
            processQueuedMessages(ctx);
            return;
        }

        //otherwise, the tunnel is not yet established, so we need to read the request headers to
        //choose a backend host to establish it.  Once established, any queued messages will be sent to the
        //backend host
        if (msg instanceof Request) {

            Request request = (Request) msg;

            this.originServerHost = originHostResolver.getOriginHost(request);

            if (this.originServerHost == null) {
                ERROR_RESPONDER.respondWithError(HttpResponseStatus.INTERNAL_SERVER_ERROR, ctx);
            }

            connectToDestination(ctx, this.originServerHost);
        }

        //else the message is a fragment/chunk after the initial HttpRequest fragment.  This fragment is already
        //queued above, so just return - it will be processed when the tunnel is established.
    }

    private void tunnelEstablished(ChannelHandlerContext ctx) throws Exception {
        tunnelEstablished = true;
        //immediately process any buffered request chunks:
        processQueuedMessages(ctx);
    }

    private void processQueuedMessages(ChannelHandlerContext ctx) throws Exception {
        HttpObject msg;

        while ((msg = messageQueue.poll()) != null) {
            tunnelRead(ctx, msg);
        }
    }

    /**
     * Invoked when a tunnel has already been established between the http client and origin http server, and the
     * frontend channel has read a message from the peer.  The semantics of this method are identical to
     * {@link #channelRead(io.netty.channel.ChannelHandlerContext, Object)}, but it will only be invoked when
     * a tunnel has been established.
     */
    private void tunnelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof Request) {

            final Request request = (Request) msg;
            final Headers headers = request.getHeaders();

            if (headers instanceof MutableHeaders) {

                MutableHeaders mutableHeaders = (MutableHeaders) headers;

                // ============== Host =============================

                final String originalHostHeaderValue = headers.getValue(HOST);

                // HTTP Proxies (like this gateway) often replace the HOST header value with the that of the origin
                // server it connects to and retain the originally-requested host in the 'X-Forwarded-Host' header.
                //
                // However, many application developers find this annoying/frustrating, since the headers aren't
                // as they expect.  As such, we turn this feature off _unless_ an upstream proxy has already
                // triggered this behavior. If so, we'll conform.
                //
                // TODO: Enable or disable these following two lines based on a configuration property:
                //String newHostHeaderValue = toHttpHostString(this.originServerHost);
                //mutableHeaders.setValue(HOST, newHostHeaderValue);

                // ============== X-Forwarded-Host =================

                String xForwardedHost = headers.getValue(X_FORWARDED_HOST);

                //don't overwrite the original xForwardedHost value if it exists:
                if (!StringUtils.hasText(xForwardedHost) && StringUtils.hasText(originalHostHeaderValue)) {
                    mutableHeaders.setValue(X_FORWARDED_HOST, originalHostHeaderValue);
                }

                // ============== X-Forwarded-For ==================

                String remoteHost = getHost(ctx.channel().remoteAddress()).getName();

                String xForwardedFor = headers.getValue(X_FORWARDED_FOR);

                if (StringUtils.hasText(xForwardedFor)) {
                    xForwardedFor += ", " + remoteHost;
                } else {
                    xForwardedFor = remoteHost;
                }

                mutableHeaders.setValue(X_FORWARDED_FOR, xForwardedFor);
            }

            //RequestEvent event = new RequestEvent(-1, buf.readableBytes());
            //this.eventBus.post(event);
        }

        if (msg instanceof NettyHttpObjectSource) {
            NettyHttpObjectSource src = (NettyHttpObjectSource) msg;
            msg = src.getHttpObject();
        }

        if (originServerChannel.isActive()) {
            originServerChannel.writeAndFlush(msg).addListener(future -> {
                if (future.isSuccess()) {
                    // was able to flush out data, start to read the next chunk
                    ctx.channel().read();
                } else {
                    ((ChannelFuture) future).channel().close(); //not successful - close the outbound channel;
                }
            });
        }
    }

    @SuppressWarnings("UnusedDeclaration") //used by commented-out code (see X-Forwarded-For code in 'tunnelRead' above)
    private static String toHttpHostString(Host host) {
        String s = host.getName();
        if (host.getPort() != 80) {
            s += ":" + host.getPort();
        }
        return s;
    }

    private static Host getHost(SocketAddress addr) {

        if (addr instanceof InetSocketAddress) {
            InetSocketAddress inetAddr = (InetSocketAddress) addr;
            String host = inetAddr.getHostString();
            int port = inetAddr.getPort();
            return new DefaultHost(host, port);
        }

        return HostParser.INSTANCE.parse(addr.toString());
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (originServerChannel != null) {
            flushAndClose(originServerChannel);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Unexpected exception.", cause);
        flushAndClose(ctx.channel());
    }

    /** Closes the specified channel after all queued write requests are flushed. */
    static void flushAndClose(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
