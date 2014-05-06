package io.darkstar;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component("frontendInitializer")
public class FrontendInitializer extends ChannelInitializer<SocketChannel> {

    @Autowired
    protected ApplicationContext appCtx;

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        initPipeline(p);
    }

    protected final void initPipeline(ChannelPipeline p) {
        p.addLast("frontendHttpRequestDecoder", new HttpRequestDecoder());
        p.addLast("frontendHttpResponseEncoder", new HttpResponseEncoder());

        FrontendHttpHandler handler = appCtx.getBean(FrontendHttpHandler.class); //must be prototype scoped
        p.addLast("frontendHandler", handler);
    }
}
