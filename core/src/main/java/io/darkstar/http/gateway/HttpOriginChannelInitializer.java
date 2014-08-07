package io.darkstar.http.gateway;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;

public class HttpOriginChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final Channel gatewayChannel;

    public HttpOriginChannelInitializer(Channel gatewayChannel) {
        this.gatewayChannel = gatewayChannel;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {

        ChannelPipeline p = ch.pipeline();

        p.addLast("originHttpRequestEncoder", new HttpRequestEncoder());
        p.addLast("originHttpResponseDecoder", new HttpResponseDecoder());

        HttpOriginHandler originHandler = new HttpOriginHandler(this.gatewayChannel);
        p.addLast("originHandler", originHandler);
    }

}

