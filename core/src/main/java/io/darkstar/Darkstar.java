package io.darkstar;

import io.darkstar.config.spring.yaml.YamlBeanDefinitionReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;

@SuppressWarnings("unchecked")
public class Darkstar {

    private static final Logger log = LoggerFactory.getLogger(Darkstar.class);

    private final GenericApplicationContext appCtx;
    private final String configFileLocation;
    private final long startMillis;

    private Darkstar(long startMillis, String configFileLocation) {
        this.startMillis = startMillis;
        this.configFileLocation = configFileLocation;
        this.appCtx = new GenericApplicationContext();
    }

    public void run() throws Exception {

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                log.info("Darkstar shutting down...");
                appCtx.close();
                log.info("Darkstar shut down in {} ms", (System.currentTimeMillis() - start));
            }
        });

        //scan for classes:
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(appCtx);
        scanner.scan("io.darkstar");

        //handle YAML config:
        YamlBeanDefinitionReader yamlReader = new YamlBeanDefinitionReader(appCtx);
        yamlReader.loadBeanDefinitions(new FileSystemResource(configFileLocation));

        //start:
        appCtx.refresh();

        long duration = System.currentTimeMillis() - this.startMillis;
        log.info("Darkstar started in {} ms", duration);
    }

    public static void main(String[] args) throws Exception {

        log.info("Darkstar starting...");

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

        final String configFileLocation = applyUserHome(yamlFilePath);

        try {
            new Darkstar(startupMillis, configFileLocation).run();
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
