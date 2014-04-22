package io.darkstar.tls;

import io.darkstar.FrontendInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

@Component
public class TlsFrontendInitializer extends FrontendInitializer {

    @Autowired
    private SSLContext sslContext;

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        SSLEngine engine = sslContext.createSSLEngine();
        engine.setUseClientMode(false);

        String[] suites = engine.getSupportedCipherSuites();
        StringBuilder sb = new StringBuilder("Supported Cipher Suites:").append("\n");
        for(String s : suites) {
            sb.append(s).append("\n");
        }
        System.out.println(sb.toString());

        suites = engine.getEnabledCipherSuites();
        sb = new StringBuilder("ENABLED Cipher Suites:").append("\n");
        for(String s : suites) {
            sb.append(s).append("\n");
        }
        System.out.println(sb.toString());

        p.addLast("ssl", new SslHandler(engine));

        initPipeline(p);
    }
}
