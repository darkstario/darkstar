package com.stormpath.monban.config.spring;

import com.codahale.metrics.MetricRegistry;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.stormpath.monban.FrontendHttpHandler;
import com.stormpath.monban.MonbanInitializer;
import com.stormpath.monban.config.DefaultHostFactory;
import com.stormpath.monban.config.Host;
import com.stormpath.monban.config.HostFactory;
import com.stormpath.monban.config.json.Config;
import com.stormpath.monban.config.json.StormpathConfig;
import com.stormpath.monban.config.json.VirtualHostConfig;
import com.stormpath.monban.ducksboard.DucksboardPoster;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.cache.Caches;
import com.stormpath.sdk.client.Client;
import com.stormpath.sdk.client.ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Configuration
public class MonbanConfig {

    public static Config JSON_CONFIG;

    @Bean
    public Config jsonConfig() {
        return JSON_CONFIG;
    }

    @Bean
    public int localPort() {
        return JSON_CONFIG.getListen().getPort();
    }

    @Bean(destroyMethod = "shutdown")
    public Executor eventBusExecutor() {
        int numProcs = Runtime.getRuntime().availableProcessors();
        return Executors.newFixedThreadPool(numProcs);
    }

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        int numProcs = Runtime.getRuntime().availableProcessors();
        scheduler.setPoolSize(numProcs * 2);
        return scheduler;

        /*
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(numProcs);
        executor.setAllowCoreThreadTimeOut(false);
        executor.setMaxPoolSize(numProcs * 3);
        executor.setKeepAliveSeconds(300); // 5 min, but core pool won't ever go below numProcs
        return executor;
        */
    }

    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService eventListenerExecutor() {
        int numProcs = Runtime.getRuntime().availableProcessors();
        return Executors.newScheduledThreadPool(numProcs);
    }

    @Bean
    public HostFactory hostFactory() {
        return new DefaultHostFactory();
    }

    @Bean
    public EventBus eventBus() {
        return new AsyncEventBus(eventBusExecutor());
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public MetricRegistry metricRegistry() {
        return new MetricRegistry();
    }

    @Bean
    public DucksboardPoster ducksboardPoster() {
        return new DucksboardPoster(ducksboardApiKey(), restTemplate());
    }

    @Bean
    public Client stormpathClient() {
        StormpathConfig stormpath = stormpathConfig();
        String apiKeyFilePath = stormpath.getApiKeyFile();
        apiKeyFilePath = applyUserHome(apiKeyFilePath);
        return new ClientBuilder()
                .setApiKeyFileLocation(apiKeyFilePath)
                .setCacheManager(Caches.newCacheManager()
                        .withCache(Caches.forResource(Application.class)
                                .withTimeToLive(2, TimeUnit.HOURS)
                                .withTimeToIdle(30, TimeUnit.MINUTES))
                        .build())
                .build();
    }

    @Bean
    public Application stormpathApplication() {
        String href = stormpathConfig().getApplicationHref();
        Application application = stormpathClient().getResource(href, Application.class);
        //force a query to ensure it is cached:
        application.getName();
        return application;
    }

    @Bean
    @Scope("prototype")
    public FrontendHttpHandler frontendHttpHandler() {
        return new FrontendHttpHandler(originHost(), eventBus(), vhostConfig(), stormpathApplication());
    }

    @Bean
    public Host originHost() {
        String hostString = vhostConfig().getBalance().getMembers().iterator().next();
        return hostFactory().getHost(hostString);
    }

    public VirtualHostConfig vhostConfig() {
        return JSON_CONFIG.getVhosts().iterator().next();
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

    private StormpathConfig stormpathConfig() {
        return vhostConfig().getStormpath();
    }

    private String ducksboardApiKey() {
        return vhostConfig().getDucksboard().getApiKey();
    }

    private String datadogApiKey() {
        return vhostConfig().getDatadog().getApiKey();
    }


}
