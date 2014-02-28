package com.stormpath.monban;

import com.google.common.eventbus.EventBus;
import com.stormpath.monban.config.VirtualHostConfig;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.client.Client;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
//import io.netty.handler.logging.LogLevel;
//import io.netty.handler.logging.LoggingHandler;

public class MonbanInitializer extends ChannelInitializer<SocketChannel> {

    private final String remoteHost;
    private final int remotePort;
    private final EventBus eventBus;
    private final VirtualHostConfig vhostConfig;
    private final Application application;

    public MonbanInitializer(String remoteHost, int remotePort, EventBus eventBus, VirtualHostConfig vhostConfig, Application application) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.eventBus = eventBus;
        this.vhostConfig = vhostConfig;
        this.application = application;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        // Uncomment the following line if you want HTTPS
        //SSLEngine engine = SecureChatSslContextFactory.getServerContext().createSSLEngine();
        //engine.setUseClientMode(false);
        //p.addLast("ssl", new SslHandler(engine));

        //p.addLast("logger", new LoggingHandler(LogLevel.INFO));
        p.addLast("httpRequestDecoder", new HttpRequestDecoder());
        p.addLast("frontendHandler", new FrontendHttpHandler(remoteHost, remotePort, this.eventBus, this.vhostConfig,  this.application));
    }
}
