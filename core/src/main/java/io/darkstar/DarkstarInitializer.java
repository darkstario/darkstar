package io.darkstar;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class DarkstarInitializer extends ChannelInitializer<SocketChannel> {

    @Autowired
    protected ApplicationContext appCtx;

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        // Uncomment the following line if you want HTTPS
        //SSLEngine engine = SecureChatSslContextFactory.getServerContext().createSSLEngine();
        //engine.setUseClientMode(false);
        //p.addLast("ssl", new SslHandler(engine));

        //p.addLast("logger", new LoggingHandler(LogLevel.INFO));
        p.addLast("httpRequestDecoder", new HttpRequestDecoder());

        FrontendHttpHandler handler = appCtx.getBean(FrontendHttpHandler.class); //must be prototype scoped
        p.addLast("frontendHandler", handler);
    }
}
