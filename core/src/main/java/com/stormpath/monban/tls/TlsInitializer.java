package com.stormpath.monban.tls;

import com.stormpath.monban.FrontendHttpHandler;
import com.stormpath.monban.MonbanInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.ssl.SslHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

@Component
public class TlsInitializer extends MonbanInitializer {

    @Autowired
    private SSLContext sslContext;

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        SSLEngine engine = sslContext.createSSLEngine();
        engine.setUseClientMode(false);
        p.addLast("ssl", new SslHandler(engine));

        //p.addLast("logger", new LoggingHandler(LogLevel.INFO));
        p.addLast("httpRequestDecoder", new HttpRequestDecoder());

        FrontendHttpHandler handler = appCtx.getBean(FrontendHttpHandler.class); //must be prototype scoped
        p.addLast("frontendHandler", handler);
    }
}
