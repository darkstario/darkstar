package io.darkstar;

import com.google.common.eventbus.EventBus;
import io.darkstar.event.BytesEvent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class BackendHandler extends ChannelHandlerAdapter {

    private final Channel inboundChannel;
    private final EventBus eventBus;

    public BackendHandler(Channel inboundChannel, EventBus eventBus) {
        this.inboundChannel = inboundChannel;
        this.eventBus = eventBus;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.read();
        ctx.write(Unpooled.EMPTY_BUFFER);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) msg;
            int count = buf.readableBytes();
            if (count > 0) {
                BytesEvent event = new BytesEvent(count, false);
                this.eventBus.post(event);
            }
        }

        inboundChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    ctx.channel().read();
                } else {
                    future.channel().close();
                }
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        FrontendHttpHandler.closeOnFlush(inboundChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        FrontendHttpHandler.closeOnFlush(ctx.channel());
    }
}
