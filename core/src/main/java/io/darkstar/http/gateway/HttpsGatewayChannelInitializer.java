package io.darkstar.http.gateway;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

@SuppressWarnings("UnusedDeclaration") //referenced in config.spring.xml
@Component("httpsGatewayChannelInitializer")
public class HttpsGatewayChannelInitializer extends HttpGatewayChannelInitializer {

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
