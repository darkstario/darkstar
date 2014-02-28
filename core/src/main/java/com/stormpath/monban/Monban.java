package com.stormpath.monban;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.stormpath.monban.config.Config;
import com.stormpath.monban.config.StormpathConfig;
import com.stormpath.monban.config.VirtualHostConfig;
import com.stormpath.monban.event.RequestListener;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.cache.Caches;
import com.stormpath.sdk.client.Client;
import com.stormpath.sdk.client.ClientBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Monban {

    private final int localPort;
    private final String remoteHost;
    private final int remotePort;
    private final EventBus eventBus;
    private final VirtualHostConfig vhostConfig;
    private final RequestListener requestListener;

    private final Config config;
    private Client stormpathClient;
    private Application application;

    public Monban(Config config) {
        this.config = config;
        this.localPort = config.getListen().getPort();

        if (config.getVhosts() == null || config.getVhosts().isEmpty()) {
            throw new IllegalArgumentException("vhosts config is required, and it must contain exactly one entry.");
        }
        VirtualHostConfig vhost = this.vhostConfig = config.getVhosts().iterator().next();

        String member = vhost.getBalance().getMembers().iterator().next();

        int colonIndex = member.lastIndexOf(':');
        if (colonIndex > 0) {
            this.remoteHost = member.substring(0, colonIndex);
            String portString = member.substring(colonIndex + 1);
            this.remotePort = Integer.parseInt(portString);
        } else {
            this.remoteHost = member;
            this.remotePort = 80;
        }

        this.eventBus = new AsyncEventBus(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
        this.requestListener = new RequestListener();
        this.eventBus.register(this.requestListener);

        StormpathConfig stormpath = vhost.getStormpath();
        if (stormpath != null) {
            String apiKeyFilePath = stormpath.getApiKeyFile();
            apiKeyFilePath = applyUserHome(apiKeyFilePath);
            this.stormpathClient = new ClientBuilder()
                    .setApiKeyFileLocation(apiKeyFilePath)
                    .setCacheManager(Caches.newCacheManager()
                            .withCache(Caches.forResource(Application.class)
                                    .withTimeToLive(2, TimeUnit.HOURS)
                                    .withTimeToIdle(30, TimeUnit.MINUTES))
                            .build())
                    .build();
            this.application = this.stormpathClient.getResource(stormpath.getApplicationHref(), Application.class);
            this.application.getName();
        }
    }

    public void run() throws Exception {
        System.out.println("Proxying *:" + localPort + " to " + remoteHost + ':' + remotePort + " ...");

        // Configure the bootstrap.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new MonbanInitializer(remoteHost, remotePort, this.eventBus, this.vhostConfig, this.application))
                    .childOption(ChannelOption.AUTO_READ, false)
                    .bind(localPort).sync().channel().closeFuture().sync();
        } finally {
            this.requestListener.shutdown();
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {

        String configFilePath;

        if (args.length == 0) {
            configFilePath = "/etc/monban/config.json";
        } else if (args.length == 1) {
            configFilePath = args[0];
        } else {
            System.err.println("Only a single (optional) argument is supported: the file path to config.json.  " +
                    "If unspecified, the default is /etc/monban/config.json.");
            return;
        }

        configFilePath = applyUserHome(configFilePath);

        ObjectMapper objectMapper = new ObjectMapper();

        File configFile = new File(configFilePath);

        Config config = objectMapper.readValue(configFile, Config.class);

        try {
            new Monban(config).run();
        } catch (Exception e) {
            System.err.println(e);
            System.exit(-1);
        }
    }

    private static String applyUserHome(String path) {
        String toReplace = "${user.home}";

        while (path.contains(toReplace)) {
            int index = path.indexOf(toReplace);
            String pre = "";
            if (index > 0) {
                pre = path.substring(0, index);
            }
            String post = path.substring(index + toReplace.length());
            String home = System.getProperty("user.home");
            path = pre + home + post;
            System.out.println("pre: " + pre + ", home: " + home + ", post: " + post);
        }

        return path;
    }

}
