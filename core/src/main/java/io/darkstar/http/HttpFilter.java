package io.darkstar.http;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;

public abstract class HttpFilter extends ChannelHandlerAdapter {

    protected HttpRequest currentRequest;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            currentRequest = (HttpRequest)msg;
        }
        super.channelRead(ctx, msg);
    }
}
