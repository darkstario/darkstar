package io.darkstar.http.gateway;

import io.darkstar.http.DarkstarHttpRequestDecoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@SuppressWarnings("UnusedDeclaration") //referenced in config.spring.xml
@Component("httpGatewayChannelInitializer")
public class HttpGatewayChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Autowired
    protected ApplicationContext appCtx;

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        initPipeline(p);
    }

    protected final void initPipeline(ChannelPipeline p) {
        p.addLast("gatewayHttpRequestDecoder", new HttpRequestDecoder());
        p.addLast("gatewayHttpResponseEncoder", new HttpResponseEncoder());
        p.addLast("gatewayDarkstarHttpRequestDecoder", new DarkstarHttpRequestDecoder());

        HttpGatewayHandler handler = appCtx.getBean(HttpGatewayHandler.class); //must be prototype scoped!
        p.addLast("gatewayHandler", handler);
    }
}
