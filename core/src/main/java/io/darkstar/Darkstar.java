package io.darkstar;

import io.darkstar.config.Host;
import io.darkstar.tls.TlsFrontendInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.util.Map;

public class Darkstar {

    private static final Logger log = LoggerFactory.getLogger(Darkstar.class);

    private final AnnotationConfigApplicationContext appCtx;
    private final long startMillis;

    public static Map YAML;

    private Darkstar(long startMillis) {
        this.startMillis = startMillis;
        appCtx = new AnnotationConfigApplicationContext();
        appCtx.registerShutdownHook();
        appCtx.scan("io.darkstar");
        appCtx.refresh();
    }

    public void run() throws Exception {

        final Host host = appCtx.getBean("defaultHost", Host.class);
        System.out.println("defaultHost: " + host);

        // Configure the bootstrap.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {

            Map http = (Map) YAML.get("http");
            Map tls = (Map) http.get("tls");
            if (tls != null) {
                int tlsPort = (int) tls.get("port");
                ServerBootstrap ssl = new ServerBootstrap();
                ssl.option(ChannelOption.SO_BACKLOG, 1024);
                ssl.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(appCtx.getBean("tlsFrontendInitializer", TlsFrontendInitializer.class))
                        .childOption(ChannelOption.AUTO_READ, false)
                        .bind(host.getName(), tlsPort);
            }

            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            ChannelFuture f = b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(appCtx.getBean("frontendInitializer", FrontendInitializer.class))
                    .childOption(ChannelOption.AUTO_READ, false)
                    .bind(host.getName(), host.getPort());

            long duration = System.currentTimeMillis() - this.startMillis;

            log.info("Darkstar started in {} ms. Listening on {}...", duration, host);

            f.sync().channel().closeFuture().sync();

        } finally {
            workerGroup.shutdownGracefully().syncUninterruptibly();
            bossGroup.shutdownGracefully().syncUninterruptibly();
            appCtx.close();
        }
    }

    public static void main(String[] args) throws Exception {

        long startupMillis = System.currentTimeMillis();

        String yamlFilePath;

        if (args.length == 0) {
            yamlFilePath = "/etc/darkstar/config.yaml";
        } else if (args.length == 1) {
            yamlFilePath = args[0];
        } else {
            System.err.println("Only a single (optional) argument is supported: the file path to config.yaml.  " +
                    "If unspecified, the default is /etc/darkstar/config.yaml.");
            return;
        }

        yamlFilePath = applyUserHome(yamlFilePath);

        Yaml yaml = new Yaml();
        Map m = (Map) yaml.load(new FileReader(new File(yamlFilePath)));
        YAML = m;

        try {
            new Darkstar(startupMillis).run();
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
