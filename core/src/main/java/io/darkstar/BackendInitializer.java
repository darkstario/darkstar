package io.darkstar;

import com.google.common.eventbus.EventBus;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;

public class BackendInitializer extends ChannelInitializer<SocketChannel> {

    private final Channel inboundChannel;
    private EventBus eventBus;

    public BackendInitializer(Channel inboundChannel, EventBus eventBus) {
        this.inboundChannel = inboundChannel;
        this.eventBus = eventBus;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {

        ChannelPipeline p = ch.pipeline();

        p.addLast("backendHttpRequestEncoder", new HttpRequestEncoder());
        p.addLast("backendHttpResponseDecoder", new HttpResponseDecoder());

        BackendHandler backendHandler = new BackendHandler(this.inboundChannel, this.eventBus);
        p.addLast("backendHandler", backendHandler);
    }

}
