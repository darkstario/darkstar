package io.darkstar;

import com.google.common.eventbus.EventBus;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class BackendHandler extends ChannelHandlerAdapter {

    private final Channel frontendChannel;
    private final EventBus eventBus;

    public BackendHandler(Channel frontendChannel, EventBus eventBus) {
        this.frontendChannel = frontendChannel;
        this.eventBus = eventBus;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //connection to the origin (backend) server has been established, so indicate that
        //we can start reading data (the http response):
        ctx.read();
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        //backend channel is receiving data from the origin server; relay it to the frontend channel so that it
        //may be delivered to the client:
        frontendChannel.writeAndFlush(msg).addListener(future -> {
            if (future.isSuccess()) {
                ctx.channel().read();
            } else {
                ((ChannelFuture)future).channel().close();
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        FrontendHttpHandler.flushAndClose(frontendChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        FrontendHttpHandler.flushAndClose(ctx.channel());
    }
}
