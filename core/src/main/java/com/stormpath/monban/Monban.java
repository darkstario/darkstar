package com.stormpath.monban;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stormpath.monban.config.json.Config;
import com.stormpath.monban.config.spring.MonbanConfig;
import io.netty.bootstrap.ServerBootstrap;
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

    public Monban(Config config) {
        MonbanConfig.JSON_CONFIG = config;
        appCtx = new AnnotationConfigApplicationContext();
        appCtx.registerShutdownHook();
        appCtx.scan("com.stormpath.monban");
        appCtx.refresh();
    }

    public void run() throws Exception {

        final int localPort = appCtx.getBean("localPort", Integer.class);

        log.info("Listening on port {}...", localPort);

        // Configure the bootstrap.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(appCtx.getBean(MonbanInitializer.class))
                    .childOption(ChannelOption.AUTO_READ, false)
                    .bind(localPort).sync().channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            appCtx.close();
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
