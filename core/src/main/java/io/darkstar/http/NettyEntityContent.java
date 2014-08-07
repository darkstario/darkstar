package io.darkstar.http;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.HttpObject;
import org.springframework.util.Assert;

public class NettyEntityContent implements EntityContent, NettyHttpObjectSource<HttpContent> {

    private final HttpContent NETTY_CONTENT;

    public NettyEntityContent(HttpContent nettyContent) {
        Assert.notNull(nettyContent, "HttpContent argument cannot be null.");
        NETTY_CONTENT = nettyContent;
    }

    @Override
    public byte[] getBytes() {
        ByteBuf buf = NETTY_CONTENT.content();
        if (buf == null || !buf.isReadable()) {
            return new byte[0];
        }

        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);

        return bytes;
    }

    @Override
    public HttpContent getHttpObject() {
        return NETTY_CONTENT;
    }
}
