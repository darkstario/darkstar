package com.stormpath.monban;

import com.google.common.base.Charsets;
import com.google.common.eventbus.EventBus;
import com.stormpath.monban.config.Host;
import com.stormpath.monban.config.json.StormpathConfig;
import com.stormpath.monban.config.json.VirtualHostConfig;
import com.stormpath.monban.event.RequestEvent;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.authc.AuthenticationResult;
import com.stormpath.sdk.authc.UsernamePasswordRequest;
import com.stormpath.sdk.resource.ResourceException;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.util.AntPathMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static io.netty.handler.codec.http.HttpConstants.*;
import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpVersion.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FrontendHttpHandler extends ChannelHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(FrontendHttpHandler.class);

    @Autowired
    private EventBus eventBus;

    @Autowired
    private StormpathConfig stormpathConfig;

    @Autowired
    private Application application;

    @Resource
    @Qualifier("virtualHosts")
    private Map<String, VirtualHostConfig> virtualHosts;

    private Channel outboundChannel;

    //whether or not a frontend-to-backend tunnel is established
    private volatile boolean tunnelEstablished;

    private AntPathMatcher pathMatcher;

    private HttpRequest request;
    private URI requestUri;
    private Host requestedHost;
    private Host destinationHost;

    private Queue<HttpObject> messageQueue;

    private ByteBuf buf;

    public FrontendHttpHandler() {
        this.pathMatcher = new AntPathMatcher();
        this.messageQueue = new ConcurrentLinkedQueue<>();
    }

    protected void ensureBuf(ChannelHandlerContext ctx) {
        if (buf == null || buf.refCnt() == 0) {
            buf = ctx.alloc().buffer(8 * 1024);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //start reading data immediately: we need to be able to read the inbound request headers to determine
        //which backend origin server to connect to:
        ctx.channel().read();
    }

    private void connectToDestination(final ChannelHandlerContext ctx, Host destination) throws Exception {

        final Channel inboundChannel = ctx.channel();

        // Start the connection attempt.
        Bootstrap b = new Bootstrap();
        b.group(inboundChannel.eventLoop())
                .channel(ctx.channel().getClass())
                .handler(new BackendHandler(inboundChannel, this.eventBus))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .option(ChannelOption.AUTO_READ, false);

        ChannelFuture f = b.connect(destination.getName(), destination.getPort());

        outboundChannel = f.channel();

        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    tunnelEstablished(ctx);
                } else {
                    //can't connect to backend - show error:
                    sendError(ctx, HttpResponseStatus.SERVICE_UNAVAILABLE, null);
                }
            }
        });
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {

        if (!(msg instanceof HttpObject) || !(((HttpObject) msg).getDecoderResult().isSuccess())) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST, null);
        }

        //noinspection ConstantConditions
        HttpObject httpObject = (HttpObject)msg;
        messageQueue.add(httpObject);

        if (tunnelEstablished) {
            processQueuedMessages(ctx);
            return;
        }

        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest)msg;
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

            VirtualHostConfig vhost = virtualHosts.get(requestedHost.getName());
            if (vhost == null) {
                //there is no vhost configured that matches the client's requested host - reject the request:
                sendError(ctx, HttpResponseStatus.SERVICE_UNAVAILABLE, null);
                return;
            }

            destinationHost = parseHost(vhost.getBalance().getMembers().iterator().next());

            connectToDestination(ctx, destinationHost);
        }
    }

    private void tunnelEstablished(ChannelHandlerContext ctx) throws Exception {
        tunnelEstablished = true;
        //immediately process any buffered request chunks:
        processQueuedMessages(ctx);
    }

    private void processQueuedMessages(ChannelHandlerContext ctx) throws Exception {
        HttpObject msg;
        while((msg = messageQueue.poll()) != null) {
            onTunnelMessage(ctx, msg);
        }
    }

    private void onTunnelMessage(final ChannelHandlerContext ctx, Object msg) throws Exception {

        Object outboundMsg = msg;

        ensureBuf(ctx);

        if (msg instanceof HttpRequest) {
            request = (HttpRequest)msg;

            //check to see if authc required:
            boolean authcRequired = false;
            String decodedPath = requestUri.getPath();
            for (String pattern : stormpathConfig.getAuthenticate()) {
                if (pathMatcher.match(pattern, decodedPath)) {
                    authcRequired = true;
                    break;
                }
            }

            buf.writeBytes(ascii(request.getMethod()));
            buf.writeByte(SP);
            buf.writeBytes(utf8(request.getUri()));
            buf.writeByte(SP);
            buf.writeBytes(ascii(request.getProtocolVersion()));
            appendCrlf(buf);

            String authzHeaderValue = null;
            boolean xForwardedForSet = false;
            boolean xForwardedHostSet = false;

            // TODO: enable lb member selection algorithm:
            writeHeader(buf, HOST.toString(), toHttpHostString(destinationHost));

            HttpHeaders headers = request.headers();

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

                    writeHeader(buf, headerName, headerValue);
                }
            }

            if (!xForwardedHostSet) {
                String headerValue = toHttpHostString(requestedHost);
                writeHeader(buf, "X-Forwarded-Host", headerValue);
            }

            if (!xForwardedForSet) {
                writeHeader(buf, "X-Forwarded-For", getHost(ctx.channel().remoteAddress()).getName());
            }

            boolean writeAuthzHeader = authzHeaderValue != null;

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

                    UsernamePasswordRequest request = new UsernamePasswordRequest(username, password, clientHost);
                    try {
                        AuthenticationResult result = this.application.authenticateAccount(request);
                        //no exception - authenticated successfully, so allow the request to continue, and
                        //replace the authz header with the authenticated account href
                        writeHeader(buf, "X-Forwarded-User", result.getAccount().getHref());
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

            if (writeAuthzHeader) {
                writeHeader(buf, AUTHORIZATION, authzHeaderValue);
            }

            appendCrlf(buf);

            outboundMsg = buf;

            RequestEvent event = new RequestEvent(-1, buf.readableBytes());
            this.eventBus.post(event);
        }

        if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;
            outboundMsg = content.content();
        }

        if (outboundChannel.isActive()) {
            outboundChannel.writeAndFlush(outboundMsg).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        // was able to flush out data, start to read the next chunk
                        ctx.channel().read();
                    } else {
                        future.channel().close(); //not successful - close the outbound channel;
                    }
                }
            });
        }
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
            return new Host(requestUri.getHost(), port);
        }

        return parseHost(hostHeaderValue);
    }

    private static Host parseHost(String hostString) {
        if (hostString == null) {
            return null;
        }

        String hostName = hostString;
        int port = 80;
        int i = hostString.lastIndexOf(':');
        if (i >= 0) {
            hostName = hostString.substring(0, i);
            String portString = hostString.substring(i + 1);
            port = Integer.parseInt(portString);
        }
        return new Host(hostName, port);
    }

    private static Host getHost(SocketAddress addr) {

        if (addr instanceof InetSocketAddress) {
            InetSocketAddress inetAddr = (InetSocketAddress) addr;
            String host = inetAddr.getHostString();
            int port = inetAddr.getPort();
            return new Host(host, port);
        }

        return parseHost(addr.toString());
    }

    private static void writeHeader(ByteBuf buf, CharSequence headerName, Object headerValue) {
        buf.writeBytes(utf8(headerName.toString())).writeByte(COLON).writeBytes(utf8(String.valueOf(headerValue)));
        appendCrlf(buf);
    }

    private static ByteBuf appendCrlf(ByteBuf buf) {
        buf.writeByte(CR).writeByte(LF);
        return buf;
    }

    private static byte[] ascii(Object o) {
        return String.valueOf(o).getBytes(Charsets.US_ASCII);
    }

    private static byte[] utf8(String o) {
        return String.valueOf(o).getBytes(Charsets.UTF_8);
    }

    private void sendBasicAuthcChallenge(ChannelHandlerContext ctx) {
        String wwwAuthcValue = "BASIC realm=\"" + this.application.getName() + "\"";
        sendError(ctx, HttpResponseStatus.UNAUTHORIZED, wwwAuthcValue);
    }

    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status, /*HACK*/ String wwwAuthcValue) {

        final ByteBuf responseBuf = ctx.alloc().buffer(512);

        //status line:
        responseBuf.writeBytes(ascii(HTTP_1_1)).writeByte(SP).writeBytes(ascii(status));
        appendCrlf(responseBuf);

        //headers:
        //HACK:
        if (status.equals(HttpResponseStatus.UNAUTHORIZED) && wwwAuthcValue != null) {
            writeHeader(responseBuf, WWW_AUTHENTICATE, wwwAuthcValue);
        }

        writeHeader(responseBuf, CONTENT_TYPE, "text/plain; charset=UTF-8");

        //body separator:
        appendCrlf(responseBuf);

        //body:
        responseBuf.writeBytes(ascii(status));
        appendCrlf(responseBuf);

        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(responseBuf).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                try {
                    future.channel().close();
                } finally {
                    release(responseBuf);
                }
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
        release(buf);
        buf = null;

        if (outboundChannel != null) {
            closeOnFlush(outboundChannel);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
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