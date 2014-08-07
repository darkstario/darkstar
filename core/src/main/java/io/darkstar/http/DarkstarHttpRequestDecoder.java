package io.darkstar.http;

import io.darkstar.net.DefaultHost;
import io.darkstar.net.Host;
import io.darkstar.net.HostParser;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.net.URI;
import java.net.URISyntaxException;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;

/**
 * Wraps Netty-specific HTTP objects (like {@link HttpRequest}, {@link HttpContent}) with functionality-enriched
 * counterparts ({@link Request}, {@link EntityContent}).  The wrapper/enriched objects will be sent down the pipeline
 * for easier Filter or Handler consumption.
 */
public class DarkstarHttpRequestDecoder extends ChannelHandlerAdapter {

    private final NettyHttpErrorResponder ERROR_RESPONDER = new NettyHttpErrorResponder();

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {

        if (!(msg instanceof HttpObject) || !(((HttpObject) msg).getDecoderResult().isSuccess())) {
            ERROR_RESPONDER.respondWithError(HttpResponseStatus.BAD_REQUEST, ctx);
            return;
        }

        if (msg instanceof HttpRequest) {

            HttpRequest request = (HttpRequest) msg;

            URI uri;
            Host host;

            try {
                uri = new URI(request.getUri());
            } catch (URISyntaxException e) {
                ERROR_RESPONDER.respondWithError(HttpResponseStatus.BAD_REQUEST, ctx);
                return;
            }

            String hostHeaderValue = request.headers().get(HOST);

            try {
                host = getRequestedHost(uri, hostHeaderValue);
            } catch (Exception e) {
                ERROR_RESPONDER.respondWithError(HttpResponseStatus.BAD_REQUEST, ctx);
                return;
            }

            msg = new NettyRequest(request, uri, host);
        }

        if (msg instanceof HttpContent) {
            HttpContent nettyContent = (HttpContent)msg;
            msg = new NettyEntityContent(nettyContent);
        }

        ctx.fireChannelRead(msg);
    }

    protected Host getRequestedHost(URI requestUri, String hostHeaderValue) {

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


}
