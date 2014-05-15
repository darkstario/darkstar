package io.darkstar;

import com.google.common.base.Charsets;
import com.google.common.eventbus.EventBus;
import io.darkstar.http.UnknownVirtualHostException;
import io.darkstar.http.VirtualHost;
import io.darkstar.http.VirtualHostResolver;
import io.darkstar.net.DefaultHost;
import io.darkstar.net.Host;
import io.darkstar.net.HostParser;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.shiro.util.AntPathMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpVersion.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FrontendHttpHandler extends ChannelHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(FrontendHttpHandler.class);

    @Autowired
    private EventBus eventBus;

    @Autowired
    private VirtualHostResolver vhostResolver;

    private Channel backendChannel;

    //whether or not a frontend-to-backend tunnel is established
    private volatile boolean tunnelEstablished;

    private AntPathMatcher pathMatcher;

    private URI requestUri; //request URI
    private Host requestedHost; //Host specified in the request
    private Host destinationHost; //Actual host that we connect to based on load balancing rules

    //queue inbound request fragments until a connection with the backend (a tunnel) is established
    //Once established, we relay the queued messages
    private Queue<HttpObject> messageQueue;

    public FrontendHttpHandler() {
        this.pathMatcher = new AntPathMatcher();
        this.messageQueue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //start reading data immediately: we need to be able to read the inbound request headers to determine
        //which backend origin server to connect to:
        ctx.channel().read();
    }

    private void connectToDestination(final ChannelHandlerContext ctx, Host destination) throws Exception {

        final Channel frontendChannel = ctx.channel();

        // Start the connection attempt.
        Bootstrap b = new Bootstrap();
        b.group(frontendChannel.eventLoop())
                .channel(ctx.channel().getClass())
                .handler(new BackendInitializer(frontendChannel, this.eventBus))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .option(ChannelOption.AUTO_READ, false);

        ChannelFuture f = b.connect(destination.getName(), destination.getPort());

        backendChannel = f.channel();

        f.addListener(future -> {
            if (future.isSuccess()) {
                tunnelEstablished(ctx);
            } else {
                //can't connect to backend - show error:
                sendError(ctx, HttpResponseStatus.SERVICE_UNAVAILABLE, null);
            }
        });
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {

        if (!(msg instanceof HttpObject) || !(((HttpObject) msg).getDecoderResult().isSuccess())) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST, null);
            return;
        }

        //noinspection ConstantConditions
        HttpObject httpObject = (HttpObject) msg;
        messageQueue.add(httpObject);

        if (tunnelEstablished) {
            processQueuedMessages(ctx);
            return;
        }

        //otherwise, the tunnel is not yet established, so we need to read the request headers to
        //choose a backend host to establish it.  Once established, any queued messages will be sent to the
        //backend host

        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            try {
                requestUri = new URI(request.getUri());
            } catch (URISyntaxException e) {
                sendError(ctx, HttpResponseStatus.BAD_REQUEST, null);
                return;
            }

            //HTTP Proxies (like this gateway) replace the HOST header value with the target (origin) server, and
            //retain the originally-requested host in the 'X-Forwarded-Host' header.
            String hostHeaderValue = request.headers().get(HOST);
            requestedHost = getRequestedHost(requestUri, hostHeaderValue);
            if (requestedHost == null) {
                //HTTP 1.1 spec requires that the client MUST specify either an absolute URI or the HOST header, and
                //if they don't specify either, a 400 bad request must be sent:
                sendError(ctx, HttpResponseStatus.BAD_REQUEST, null);
                return;
            }

            VirtualHost vhost;
            try {
                vhost = vhostResolver.getVirtualHost(requestedHost.getName());
            } catch (UnknownVirtualHostException e) {
                //there is no vhost configured that matches the client's requested host - reject the request:
                sendError(ctx, HttpResponseStatus.SERVICE_UNAVAILABLE, null);
                return;
            }

            //TODO determine destinationHost based on a load balancing algorithm
            destinationHost = vhost.getOriginHost();

            connectToDestination(ctx, destinationHost);
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

        Object outboundMsg = msg;

        if (msg instanceof HttpRequest) {

            final HttpRequest originalRequest = (HttpRequest) msg;

            HttpRequest modifiedRequest;
            if (msg instanceof FullHttpRequest) {
                modifiedRequest = ((FullHttpRequest) msg).duplicate();
            } else {
                modifiedRequest = new DefaultHttpRequest(originalRequest.getProtocolVersion(),
                        originalRequest.getMethod(), originalRequest.getUri(), true);
                modifiedRequest.headers().set(originalRequest.headers());
            }

            //check to see if authc required:
            boolean authcRequired = false;
            String decodedPath = requestUri.getPath();
            /*
            for (String pattern : stormpathConfig.getAuthenticate()) {
                if (pathMatcher.match(pattern, decodedPath)) {
                    authcRequired = true;
                    break;
                }
            }
            */

            String authzHeaderValue = null;
            boolean xForwardedForSet = false;
            boolean xForwardedHostSet = false;

            modifiedRequest.headers().set(HOST, toHttpHostString(destinationHost));

            HttpHeaders headers = originalRequest.headers();

            if (!headers.isEmpty()) {

                for (Map.Entry<String, String> header : headers) {

                    String headerName = header.getKey();
                    String headerValue = header.getValue();

                    if (headerName.equalsIgnoreCase("X-Forwarded-Host")) {
                        xForwardedHostSet = true; //an upstream proxy/gateway already set it
                    }

                    if (headerName.equalsIgnoreCase("X-Forwarded-For")) {
                        headerValue += ", " + getHost(ctx.channel().remoteAddress()).getName();
                        xForwardedForSet = true;
                    }

                    if (headerName.equalsIgnoreCase(HOST.toString())) {
                        //we already captured this header (above, before iterating), so skip writing this specific
                        //header, but retain the value so we can use it to set the X-Forwarded-Host header later
                        continue;
                    }

                    if (headerName.equalsIgnoreCase(AUTHORIZATION.toString())) {
                        authzHeaderValue = headerValue;
                        continue; //don't write out this particular header - we'll do that in a minute
                    }

                    modifiedRequest.headers().set(headerName, headerValue);
                }
            }

            if (!xForwardedHostSet) {
                String headerValue = toHttpHostString(requestedHost);
                //
                modifiedRequest.headers().set("X-Forwarded-Host", headerValue);
            }

            if (!xForwardedForSet) {
                modifiedRequest.headers().set("X-Forwarded-For", getHost(ctx.channel().remoteAddress()).getName());
            }

            boolean writeAuthzHeader = authzHeaderValue != null;

            /*
            if (authcRequired) {

                if (authzHeaderValue != null && authzHeaderValue.toLowerCase().trim().startsWith("basic ")) {

                    int lastSpaceIndex = authzHeaderValue.lastIndexOf(' ');
                    String schemeValue = authzHeaderValue.substring(lastSpaceIndex + 1);

                    String decoded = Base64.decodeToString(schemeValue);

                    String username;
                    String password = null;

                    int colonIndex = decoded.lastIndexOf(':');
                    if (colonIndex > 0) {
                        username = decoded.substring(0, colonIndex);

                        if (colonIndex < (decoded.length() - 1)) {
                            password = decoded.substring(colonIndex + 1);

                        } //else no password
                    } else {
                        username = decoded;
                    }

                    String clientHost = getHost(ctx.channel().remoteAddress()).getName();

                    UsernamePasswordRequest authcRequest = new UsernamePasswordRequest(username, password, clientHost);
                    try {
                        AuthenticationResult result = this.application.authenticateAccount(authcRequest);
                        //no exception - authenticated successfully, so allow the request to continue, and
                        //replace the authz header with the authenticated account href

                        modifiedRequest.headers().set("X-Forwarded-User", result.getAccount().getHref());
                        writeAuthzHeader = false;
                    } catch (ResourceException e) {
                        sendBasicAuthcChallenge(ctx);
                        return;
                    }
                } else {
                    sendBasicAuthcChallenge(ctx);
                    return;
                }
            }
            */

            if (writeAuthzHeader) {
                modifiedRequest.headers().set(AUTHORIZATION, authzHeaderValue);
            }

            outboundMsg = modifiedRequest;

            //RequestEvent event = new RequestEvent(-1, buf.readableBytes());
            //this.eventBus.post(event);
        }

        if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;
            outboundMsg = content.content();
        }

        if (backendChannel.isActive()) {
            backendChannel.writeAndFlush(outboundMsg).addListener(future -> {
                if (future.isSuccess()) {
                    // was able to flush out data, start to read the next chunk
                    ctx.channel().read();
                } else {
                    ((ChannelFuture)future).channel().close(); //not successful - close the outbound channel;
                }
            });
        }
    }

    /**
     * Invoked when the BackendHandler receives data from the origin server and relays it to the frontend channel.
     * This is only invoked after a tunnel has been established, and the backend handler is relaying responses through
     * the tunnel.
     */
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);
    }

    private static String toHttpHostString(Host host) {
        String s = host.getName();
        if (host.getPort() != 80) {
            s += ":" + host.getPort();
        }
        return s;
    }

    private static Host getRequestedHost(URI requestUri, String hostHeaderValue) {

        if (requestUri.isAbsolute()) {
            //If the requestUri is absolute, the HTTP server MUST ignore any Host header and use
            //the host in the requestUri: http://www.w3.org/Protocols/rfc2616/rfc2616-sec5.html#sec5.2
            int port = requestUri.getPort();
            if (port <= 0) {
                port = 80;
            }
            return new DefaultHost(requestUri.getHost(), port);
        }

        return HostParser.INSTANCE.parse(hostHeaderValue);
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

    private static byte[] utf8(Object o) {
        return String.valueOf(o).getBytes(Charsets.UTF_8);
    }

    /*
    private void sendBasicAuthcChallenge(ChannelHandlerContext ctx) {
        String wwwAuthcValue = "BASIC realm=\"" + this.application.getName() + "\"";
        sendError(ctx, HttpResponseStatus.UNAUTHORIZED, wwwAuthcValue);
    }
    */

    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status, /*HACK*/ String wwwAuthcValue) {

        byte[] bodyBytes = utf8(status);
        final ByteBuf body = ctx.alloc().buffer(bodyBytes.length);
        body.writeBytes(bodyBytes);

        final FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_0, status, body, true);
        //headers:
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        //HACK:
        if (wwwAuthcValue != null && status.equals(HttpResponseStatus.UNAUTHORIZED)) {
            response.headers().set(WWW_AUTHENTICATE, wwwAuthcValue);
        }

        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(future -> {
            try {
                ((ChannelFuture)future).channel().close();
            } finally {
                release(body);
            }
        });
    }

    private static void release(ByteBuf buf) {
        if (buf != null && buf.refCnt() > 0) {
            buf.release();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (backendChannel != null) {
            closeOnFlush(backendChannel);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Unexpected exception.", cause);
        closeOnFlush(ctx.channel());
    }

    /**
     * Closes the specified channel after all queued write requests are flushed.
     */
    static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}