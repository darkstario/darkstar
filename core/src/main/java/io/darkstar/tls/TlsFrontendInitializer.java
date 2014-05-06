package io.darkstar.tls;

import io.darkstar.FrontendInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

@Component("tlsFrontendInitializer")
public class TlsFrontendInitializer extends FrontendInitializer {

    @Autowired
    private SSLContext sslContext;

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        SSLEngine engine = sslContext.createSSLEngine();
        engine.setUseClientMode(false);
        p.addLast("ssl", new SslHandler(engine));

        initPipeline(p);
    }
}
