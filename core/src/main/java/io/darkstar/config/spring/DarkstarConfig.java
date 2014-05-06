package io.darkstar.config.spring;

import com.codahale.metrics.MetricRegistry;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import io.darkstar.config.DefaultHostFactory;
import io.darkstar.config.Host;
import io.darkstar.config.HostFactory;
import io.darkstar.config.json.LogConfig;
import io.darkstar.config.json.SystemLogConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestTemplate;
import reactor.core.Environment;
import reactor.core.Reactor;
import reactor.core.spec.Reactors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@SuppressWarnings("unchecked")
@Configuration
public class DarkstarConfig implements BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

        /*

        BeanDefinitionFactory<SystemLogConfig> systemLogBeanDefinitionFactory = systemLogBeanDefinitionFactory();

        SystemLogConfig defaultSystemLogConfig = Objects.newInstance(SystemLogConfig.class, YAML.get("system_log"));
        Map<String, BeanDefinition> defs = systemLogBeanDefinitionFactory.createBeanDefinitions("default", defaultSystemLogConfig);
        for (String name : defs.keySet()) {
            registry.registerBeanDefinition(name, defs.get(name));
        }

        Map http = (Map) YAML.get("http");

        BeanDefinitionFactory<LogConfig> accessLogBeanDefinitionFactory = accessLogBeanDefinitionFactory();

        LogConfig defaultAccessLogConfig = Objects.newInstance(LogConfig.class, http.get("access_log"));

        Map<String, Map> vhosts = (Map) http.get("vhosts");

        for (Map.Entry<String, Map> entry : vhosts.entrySet()) {

            String vhostName = entry.getKey();
            Map vhost = entry.getValue();

            Object vhostSystemLog = vhost.get("system_log");
            if (vhostSystemLog != null) {
                SystemLogConfig vhostSystemLogConfig = Objects.newInstance(SystemLogConfig.class, defaultSystemLogConfig);
                if (vhostSystemLog instanceof String) {
                    vhostSystemLogConfig.setPath((String) vhostSystemLog);
                } else {
                    Objects.applyProperties(vhostSystemLogConfig, vhostSystemLog);
                }
                defs = systemLogBeanDefinitionFactory.createBeanDefinitions(vhostName, vhostSystemLogConfig);
                for (String name : defs.keySet()) {
                    registry.registerBeanDefinition(name, defs.get(name));
                }
            }

            Object vhostAccessLog = vhost.get("access_log");
            if (vhostAccessLog != null) {
                LogConfig vhostAccessLogConfig = Objects.newInstance(LogConfig.class, defaultAccessLogConfig);
                if (vhostAccessLog instanceof String) {
                    vhostAccessLogConfig.setPath((String) vhostAccessLog);
                } else {
                    Objects.applyProperties(vhostAccessLogConfig, vhostAccessLog);
                }
                defs = accessLogBeanDefinitionFactory.createBeanDefinitions(vhostName, vhostAccessLogConfig);
                for (String name : defs.keySet()) {
                    registry.registerBeanDefinition(name, defs.get(name));
                }
            }
        } */
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        //no-op
    }

    /*
    @Bean
    public Node rootConfigNode() {
        return (Node)createMapNode("root", YAML, null);
    }

    private Object createNode(String name, Object o, Node parent) {
        DefaultNode node = null;
        if (name != null) {
            node = new DefaultNode(name, o, parent);
        }
        return node != null ? node : o;
    }

    private Object createMapNode(String name, Map<String,?> m, Node parent) {

        Map<String,Object> nodeValues = new LinkedHashMap<>(m.size());

        DefaultNode node = null;

        if (name != null) {
            node = new DefaultNode(name, nodeValues);
            node.setParent(parent);
        }

        for(Map.Entry<String,?> entry : m.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map) {
                Map<String,?> m2 = (Map<String,?>)value;
                nodeValues.put(key, createMapNode(key, m2, node));
            } else if (value instanceof Collection) {
                Collection c = (Collection)value;
                nodeValues.put(key, createCollectionNode(key, c, node));
            } else {
                nodeValues.put(key, createNode(key, value, node));
            }
        }

        return node != null ? node : nodeValues;
    }


    private Object createCollectionNode(String name, Collection c, Node parent) {

        List nodeValues = new ArrayList(c.size());

        DefaultNode node = null;

        if (name != null) {
            node = new DefaultNode(name, nodeValues);
            node.setParent(parent);
        }

        for(Object o : c) {
            if (o instanceof Map) {
                Map<String,?> m = (Map<String,?>)o;
                nodeValues.add(createMapNode(null, m, node));
            } else if (o instanceof Collection) {
                Collection c2 = (Collection)o;
                nodeValues.add(createCollectionNode(null, c2, node));
            } else {
                nodeValues.add(createNode(null, o, node));
            }
        }

        return node != null ? node : nodeValues;
    }
    */

    @Bean
    public BeanDefinitionFactory<SystemLogConfig> systemLogBeanDefinitionFactory() {
        return new SystemLogBeanDefinitionFactory();
    }

    @Bean
    public BeanDefinitionFactory<LogConfig> accessLogBeanDefinitionFactory() {
        return new AccessLogBeanDefinitionFactory();
    }

    @Bean
    public Host defaultHost() {
        return new Host("127.0.0.1", 5000);
        //String name = get(String.class, YAML, "['http']['name']");
        //int port = get(Integer.class, YAML, "['http']['port']");
        //return new Host(name, port);
    }

    @Bean(destroyMethod = "shutdown")
    public Environment reactorEnvironment() {
        return new Environment();
    }

    @Bean
    public Reactor reactor() {
        return Reactors.reactor().env(reactorEnvironment()).dispatcher(Environment.EVENT_LOOP).get();
    }

    @Bean(destroyMethod = "shutdown")
    public ExecutorService eventBusExecutor() {
        int numProcs = Runtime.getRuntime().availableProcessors();
        return Executors.newFixedThreadPool(numProcs * 2);
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

    /*
    @Bean
    public StormpathConfig stormpathConfig() {
        Object stormpath = get(Object.class, YAML, "['http']['vhosts']['vhost.com']['stormpath']");
        return newInstance(StormpathConfig.class, stormpath);
    }

    @Bean
    public Client stormpathClient() {
        StormpathConfig stormpath = stormpathConfig();
        String apiKeyFilePath = stormpath.getApiKeyFile();
        apiKeyFilePath = applyUserHome(apiKeyFilePath);
        return Clients.builder()
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
    */

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

    /*
    private String ducksboardApiKey() {
        return vhostConfig().getDucksboard().getApiKey();
    }

    private String datadogApiKey() {
        return vhostConfig().getDatadog().getApiKey();
    }
    */

}
