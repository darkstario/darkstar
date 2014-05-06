package io.darkstar;

import io.darkstar.config.spring.yaml.YamlBeanDefinitionReader;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;

import java.util.Map;

@SuppressWarnings("unchecked")
public class Darkstar {

    private static final Logger log = LoggerFactory.getLogger(Darkstar.class);

    private final GenericApplicationContext appCtx;
    private final String configFileLocation;
    private final long startMillis;

    public static String YAML_FILE_PATH; //todo remove

    private Darkstar(long startMillis, String configFileLocation) {

        this.startMillis = startMillis;
        this.configFileLocation = configFileLocation;

        GenericApplicationContext appCtx = new GenericApplicationContext();
        appCtx.registerShutdownHook();

        //scan for classes:
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(appCtx);
        scanner.scan("io.darkstar");

        //handle YAML config:
        YamlBeanDefinitionReader yamlReader = new YamlBeanDefinitionReader(appCtx);
        yamlReader.loadBeanDefinitions(new FileSystemResource(configFileLocation));

        //start:
        appCtx.refresh();

        this.appCtx = appCtx;
    }

    public void run() throws Exception {
        try {
            Map<String,Channel> beans = appCtx.getBeansOfType(Channel.class);
            long duration = System.currentTimeMillis() - this.startMillis;
            log.info("Darkstar started in {} ms.", duration);
            for(Channel channel : beans.values()) {
                channel.closeFuture().awaitUninterruptibly();
            }
        } finally {
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

        YAML_FILE_PATH = applyUserHome(yamlFilePath);

        try {
            new Darkstar(startupMillis, YAML_FILE_PATH).run();
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
