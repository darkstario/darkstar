package com.stormpath.monban;

import com.google.common.base.Charsets;
import com.google.common.eventbus.EventBus;
import com.stormpath.monban.config.Host;
import com.stormpath.monban.config.json.StormpathConfig;
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
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;

import static io.netty.handler.codec.http.HttpConstants.*;
import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpVersion.*;

public class FrontendHttpHandler extends ChannelHandlerAdapter {

    private final Host originHost;

    @Autowired
    private EventBus eventBus;

    @Autowired
    private StormpathConfig stormpathConfig;

    @Autowired
    private Application application;

    private Channel outboundChannel;
    private AntPathMatcher pathMatcher;

    private HttpRequest request;
    private ByteBuf buf;

    public FrontendHttpHandler(Host originHost) {
        this.originHost = originHost;
        this.pathMatcher = new AntPathMatcher();
    }

    protected void ensureBuf(ChannelHandlerContext ctx) {
        if (buf == null || buf.refCnt() == 0) {
            buf = ctx.alloc().buffer(8 * 1024);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        final Channel inboundChannel = ctx.channel();

        // Start the connection attempt.
        Bootstrap b = new Bootstrap();
        b.group(inboundChannel.eventLoop())
                .channel(ctx.channel().getClass())
                .handler(new BackendHandler(inboundChannel, this.eventBus))
                .option(ChannelOption.AUTO_READ, false);

        ChannelFuture f = b.connect(originHost.getName(), originHost.getPort());

        outboundChannel = f.channel();

        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    // connection complete start to read first data
                    inboundChannel.read();
                } else {
                    // Close the connection if the connection attempt has failed.
                    inboundChannel.close();
                }
            }
        });
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {

        Object outboundMsg = msg;

        if (msg instanceof HttpObject && !(((HttpObject) msg).getDecoderResult().isSuccess())) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST, null);
            return;
        }

        ensureBuf(ctx);

        if (msg instanceof HttpRequest) {
            request = (HttpRequest) msg;

            //check to see if authc required:
            String uri = request.getUri();
            int queryIndex = uri.indexOf('?');
            if (queryIndex > 0) { //we only care about the base path for the authc check
                uri = uri.substring(0, queryIndex);
            }

            boolean authcRequired = false;

            for (String pattern : stormpathConfig.getAuthenticate()) {
                if (pathMatcher.match(pattern, uri)) {
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

            String host = null;
            String authzHeaderValue = null;

            boolean xForwardedForSet = false;
            boolean xForwardedHostSet = false;

            HttpHeaders headers = request.headers();

            if (!headers.isEmpty()) {

                for (Map.Entry<String, String> header : headers) {

                    String headerName = header.getKey();
                    String headerValue = header.getValue();

                    if (headerName.equalsIgnoreCase("X-Forwarded-For")) {

                        headerValue += ", " + getHost(ctx.channel().remoteAddress()).getName();
                        xForwardedForSet = true;
                    }

                    if (headerName.equalsIgnoreCase("X-Forwarded-Host")) {
                        xForwardedHostSet = true;
                    }

                    if (headerName.equalsIgnoreCase(HOST.toString())) {
                        host = headerValue;
                    }

                    if (headerName.equalsIgnoreCase(AUTHORIZATION.toString())) {
                        authzHeaderValue = headerValue;
                        continue; //don't write out this particular header - we'll do that in a minute
                    }

                    writeHeader(buf, headerName, headerValue);
                }
            }

            if (!xForwardedForSet) {
                writeHeader(buf, "X-Forwarded-For", getHost(ctx.channel().remoteAddress()).getName());
            }

            if (!xForwardedHostSet && host != null) {
                writeHeader(buf, "X-Forwarded-Host", host);
            }

            boolean writeAuthzHeader = authzHeaderValue != null;

            if (authcRequired) {

                if (authzHeaderValue == null) {
                    sendBasicAuthcChallenge(ctx);
                    return;
                }

                if (authzHeaderValue.toLowerCase().trim().startsWith("basic ")) {

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

                    String clientHost = host;
                    if (clientHost == null) {
                        clientHost = getHost(ctx.channel().remoteAddress()).getName();
                    }

                    UsernamePasswordRequest request = new UsernamePasswordRequest(username, password, clientHost);
                    try {
                        AuthenticationResult result = this.application.authenticateAccount(request);
                        //no exception - authenticated successfully, so allow the request to continue, and
                        //replace the authz header with the authenticated account href
                        writeHeader(buf, "X-Stormpath-Account", result.getAccount().getHref());
                        writeAuthzHeader = false;
                    } catch (ResourceException e) {
                        sendBasicAuthcChallenge(ctx);
                        return;
                    }
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
                        future.channel().close();
                    }
                }
            });
        }
    }


    private static Host getHost(SocketAddress addr) {

        String host;
        int port = 0;

        if (addr instanceof InetSocketAddress) {
            InetSocketAddress inetAddr = (InetSocketAddress) addr;
            host = inetAddr.getHostString();
            port = inetAddr.getPort();
        } else {
            host = addr.toString();
            int index = host.lastIndexOf(':');
            if (index > 0) {
                host = host.substring(0, index);
                if (index < (host.length() - 1)) {
                    String portString = host.substring(index + 1);
                    port = Integer.parseInt(portString);
                }
            }
        }

        return new Host(host, port);
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