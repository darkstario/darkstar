package io.darkstar.http;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpVersion.*;

public class NettyHttpErrorResponder {

    public void respondWithError(HttpResponseStatus status, ChannelHandlerContext ctx) {

        byte[] bodyBytes = utf8(status);
        final ByteBuf body = ctx.alloc().buffer(bodyBytes.length);
        body.writeBytes(bodyBytes);

        final FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_0, status, body, true);

        //headers:
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

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

    private static byte[] utf8(Object o) {
        return String.valueOf(o).getBytes(Charsets.UTF_8);
    }
}
