package com.stormpath.monban;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stormpath.monban.config.Host;
import com.stormpath.monban.config.json.Config;
import com.stormpath.monban.config.json.TlsConfig;
import com.stormpath.monban.config.spring.MonbanConfig;
import com.stormpath.monban.tls.TlsInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;

public class Monban {

    private static final Logger log = LoggerFactory.getLogger(Monban.class);

    private final AnnotationConfigApplicationContext appCtx;
    private final long startMillis;

    private Monban(long startMillis, Config config) {
        this.startMillis = startMillis;
        MonbanConfig.JSON_CONFIG = config;
        appCtx = new AnnotationConfigApplicationContext();
        appCtx.registerShutdownHook();
        appCtx.scan("com.stormpath.monban");
        appCtx.refresh();
    }

    public void run() throws Exception {

        final Host host = appCtx.getBean("defaultHost", Host.class);

        final Config jsonConfig = appCtx.getBean("jsonConfig", Config.class);

        // Configure the bootstrap.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {

            TlsConfig tls = jsonConfig.getTls();
            if (tls != null) {
                ServerBootstrap ssl = new ServerBootstrap();
                ssl.option(ChannelOption.SO_BACKLOG, 1024);
                ssl.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(appCtx.getBean("tlsInitializer", TlsInitializer.class))
                        .childOption(ChannelOption.AUTO_READ, false)
                        .bind(host.getName(), tls.getPort());
            }

            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            ChannelFuture f = b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(appCtx.getBean("monbanInitializer", MonbanInitializer.class))
                    .childOption(ChannelOption.AUTO_READ, false)
                    .bind(host.getName(), host.getPort());

            long duration = System.currentTimeMillis() - this.startMillis;

            log.info("Started in {} ms. Listening on {}...", duration, host);

            f.sync().channel().closeFuture().sync();

        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            appCtx.close();
        }
    }

    public static void main(String[] args) throws Exception {

        long startupMillis = System.currentTimeMillis();

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
            new Monban(startupMillis, config).run();
        } catch (Exception e) {
            e.printStackTrace(System.err);
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
            //System.out.println("pre: " + pre + ", home: " + home + ", post: " + post);
        }

        return path;
    }
}
