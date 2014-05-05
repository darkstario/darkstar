package io.darkstar.net;

import io.darkstar.FrontendInitializer;
import io.darkstar.tls.TlsFrontendInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component("serverChannelManager")
public class DefaultServerChannelManager implements ServerChannelManager {

    @Autowired
    private ApplicationContext applicationContext;

    private final ConcurrentMap<Integer, Registration> registrations;

    private int numWorkers = 0; //0 means let netty choose based on num processors (recommended)

    private Channel lastChannel;
    private final ChannelGroup allChannels;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public DefaultServerChannelManager() {
        this.registrations = new ConcurrentHashMap<>();
        this.allChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    }

    public void setNumWorkers(int numWorkers) {
        this.numWorkers = numWorkers;
    }

    @Override
    public void registerServerChannel(Connector connector, boolean tls) {
        int port = connector.getPort();
        Registration existing = this.registrations.putIfAbsent(port, new Registration(connector, tls));
        if (existing != null) {
            throw new IllegalArgumentException("Multiple listen directives cannot currently be specified for the same port.");
        }
    }

    @Override
    public void init() {

        workerGroup = new NioEventLoopGroup(numWorkers);
        bossGroup = new NioEventLoopGroup(1);

        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.AUTO_READ, false);

        for (Registration registration : registrations.values()) {

            if (registration.isTls()) {
                bootstrap.childHandler(applicationContext.getBean("tlsFrontendInitializer", TlsFrontendInitializer.class));
            } else {
                bootstrap.childHandler(applicationContext.getBean("frontendInitializer", FrontendInitializer.class));
            }

            String address = registration.getConnector().getAddress();
            int port = registration.getConnector().getPort();

            ChannelFuture channelFuture;

            if (address != null) {
                channelFuture = bootstrap.bind(address, port);
            } else {
                channelFuture = bootstrap.bind(port);
            }

            channelFuture.awaitUninterruptibly();

            if (!channelFuture.isSuccess()) {
                throw new IllegalStateException("Unable to bind to " + registration.getConnector(), channelFuture.cause());
            }

            Channel channel = channelFuture.channel();
            lastChannel = channel;

            allChannels.add(channel);
        }
    }

    @Override
    public void sync() {
        try {
            lastChannel.closeFuture().syncUninterruptibly();
            allChannels.close().awaitUninterruptibly();
        } finally {
            bossGroup.shutdownGracefully().syncUninterruptibly();
            workerGroup.shutdownGracefully().syncUninterruptibly();
        }
    }

    private static final class Registration {

        private final Connector connector;
        private final boolean tls;

        private Registration(Connector connector, boolean tls) {
            this.connector = connector;
            this.tls = tls;
        }

        private Connector getConnector() {
            return connector;
        }

        private boolean isTls() {
            return tls;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Registration that = (Registration) o;

            return tls == that.tls && connector.equals(that.connector);
        }

        @Override
        public int hashCode() {
            int result = connector.hashCode();
            result = 31 * result + (tls ? 1 : 0);
            return result;
        }
    }
}
