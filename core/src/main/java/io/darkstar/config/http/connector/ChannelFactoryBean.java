package io.darkstar.config.http.connector;

import io.darkstar.net.DefaultHost;
import io.darkstar.net.Host;
import io.darkstar.net.HostParser;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class ChannelFactoryBean extends AbstractFactoryBean<Channel> implements ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(ChannelFactoryBean.class);

    private String address;
    private int port;

    private NioEventLoopGroup workerGroup;
    private ChannelInitializer channelInitializer;
    private ApplicationContext appCtx;

    public void setWorkerGroup(NioEventLoopGroup workerGroup) {
        this.workerGroup = workerGroup;
    }

    public void setChannelInitializer(ChannelInitializer channelInitializer) {
        this.channelInitializer = channelInitializer;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.appCtx = applicationContext;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public Class<?> getObjectType() {
        return Channel.class;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(channelInitializer, "channelInitializer is required.");
        super.afterPropertiesSet();
    }

    @Override
    protected Channel createInstance() throws Exception {

        Host host = null;

        if (StringUtils.hasText(address)) {
            host = HostParser.INSTANCE.parse(address);
            if (port > 0) {
                if (host.getPort() > 0) {
                    String msg = "Connector address embeds a port value, but the port property has also been set. " +
                            "Only one of the two may be specified.";
                    throw new BeanInitializationException(msg);
                }
                host = new DefaultHost(host.getName(), port);
            }
        } else {
            if (port > 0) {
                host = new DefaultHost(null, port);
            }
        }

        if (host == null) {
            int defaultPort = 80; //TODO: auto change to 8000 if Effective UID is not root
            host = new DefaultHost(null, defaultPort);
        }

        if (workerGroup == null) {
            workerGroup = new NioEventLoopGroup();
        }

        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);

        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                // ALLOCATOR, see:
                // - https://blog.twitter.com/2013/netty-4-at-twitter-reduced-gc-overhead
                // - https://www.youtube.com/watch?v=_GRIyCMNGGI&t=1183
                // - http://normanmaurer.me/presentations/2014-facebook-eng-netty/slides.html#14.0
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.AUTO_READ, false);

        bootstrap.childHandler(channelInitializer);

        ChannelFuture future;

        if (StringUtils.hasText(host.getName())) {
            future = bootstrap.bind(host.getName(), host.getPort());
        } else {
            future = bootstrap.bind(host.getPort());
        }

        future.sync();

        log.info("Listening on {}...", host);

        return future.channel();
    }


    @Override
    protected void destroyInstance(Channel channel) throws Exception {
        channel.close().sync();
    }
}
