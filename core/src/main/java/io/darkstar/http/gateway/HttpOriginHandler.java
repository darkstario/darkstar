package io.darkstar.http.gateway;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class HttpOriginHandler extends ChannelHandlerAdapter {

    private final Channel gatewayChannel;

    public HttpOriginHandler(Channel gatewayChannel) {
        this.gatewayChannel = gatewayChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //connection to the origin (backend) server has been established, so indicate that
        //we can start reading data (the http response):
        ctx.read();
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        //origin channel is receiving data from the origin server; relay it to the gateway channel so that it
        //may be sent to the client:
        gatewayChannel.writeAndFlush(msg).addListener(future -> {
            if (future.isSuccess()) {
                ctx.channel().read();
            } else {
                ((ChannelFuture) future).channel().close();
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        HttpGatewayHandler.flushAndClose(gatewayChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        HttpGatewayHandler.flushAndClose(ctx.channel());
    }
}