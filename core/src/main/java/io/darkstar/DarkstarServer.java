package io.darkstar;

import io.darkstar.config.spring.yaml.YamlBeanDefinitionReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;

public class DarkstarServer implements InitializingBean, DisposableBean, Lifecycle {

    private static final Logger log = LoggerFactory.getLogger(DarkstarServer.class);

    private GenericApplicationContext appCtx;
    private final String configFileLocation;
    private final long startMillis;
    private final Thread shutdownHook;
    private final String name;
    private volatile boolean running;

    public DarkstarServer(long startMillis, String configFileLocation) {
        this("Darkstar", startMillis, configFileLocation);
    }

    public DarkstarServer(final String name, long startMillis, String configFileLocation) {
        this.name = name;
        this.startMillis = startMillis;
        this.configFileLocation = configFileLocation;
        this.running = false;
        this.shutdownHook = new Thread() {
            @Override
            public void run() {
                DarkstarServer.this.stop();
            }
        };
    }

    @Override
    public void destroy() throws Exception {
        stop();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.start();
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    public synchronized void start() {
        if (this.running) {
            return;
        }

        log.info(name + " starting...");

        this.appCtx = new GenericApplicationContext();

        Runtime.getRuntime().addShutdownHook(shutdownHook);

        //scan for classes:
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(appCtx);
        scanner.scan("io.darkstar");

        //handle YAML config:
        YamlBeanDefinitionReader yamlReader = new YamlBeanDefinitionReader(appCtx);
        yamlReader.loadBeanDefinitions(new FileSystemResource(configFileLocation));

        //start:
        appCtx.refresh();

        this.running = true;

        long duration = System.currentTimeMillis() - this.startMillis;
        log.info("{} started in {} ms", name, duration);
    }

    public synchronized void stop() {
        if (!this.running) {
            return;
        }

        try {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
        } catch (IllegalStateException e) { //shutdown already in progress - ignore the error.
        }
        long start = System.currentTimeMillis();
        log.info("{} shutting down...", name);
        appCtx.close();
        this.running = false;
        log.info("{} shut down in {} ms", name, (System.currentTimeMillis() - start));
    }

}
