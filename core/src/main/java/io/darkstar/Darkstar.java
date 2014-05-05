package io.darkstar;

import io.darkstar.net.ServerChannelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@SuppressWarnings("unchecked")
public class Darkstar {

    private static final Logger log = LoggerFactory.getLogger(Darkstar.class);

    private final AnnotationConfigApplicationContext appCtx;
    private final long startMillis;

    public static String YAML_FILE_PATH; //todo remove

    private Darkstar(long startMillis) {
        this.startMillis = startMillis;
        appCtx = new AnnotationConfigApplicationContext();
        appCtx.registerShutdownHook();
        appCtx.scan("io.darkstar");
        appCtx.refresh();
    }

    public void run() throws Exception {
        try {
            ServerChannelManager serverChannelManager = appCtx.getBean(ServerChannelManager.class);
            serverChannelManager.init();
            long duration = System.currentTimeMillis() - this.startMillis;
            log.info("Darkstar started in {} ms.", duration);
            serverChannelManager.sync();
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
