package io.darkstar.config.spring;

import com.codahale.metrics.MetricRegistry;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.cache.Caches;
import com.stormpath.sdk.client.Client;
import com.stormpath.sdk.client.Clients;
import io.darkstar.Darkstar;
import io.darkstar.config.DefaultHostFactory;
import io.darkstar.config.Host;
import io.darkstar.config.HostFactory;
import io.darkstar.config.json.Config;
import io.darkstar.config.json.LogConfig;
import io.darkstar.config.json.StormpathConfig;
import io.darkstar.config.json.SystemLogConfig;
import io.darkstar.config.json.TlsConfig;
import io.darkstar.config.json.VirtualHostConfig;
import io.darkstar.lang.Objects;
import io.darkstar.tls.BouncyCastleKeyEntryFactory;
import io.darkstar.tls.KeyEntry;
import io.darkstar.tls.KeyEntryFactory;
import io.darkstar.tls.SniKeyManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
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

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509ExtendedKeyManager;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Configuration
public class DarkstarConfig implements BeanDefinitionRegistryPostProcessor {

    public static Config JSON_CONFIG;

    public static Map YAML = Darkstar.YAML;

    @SuppressWarnings("unchecked")
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

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
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        //no-op
    }

    @Bean
    public BeanDefinitionFactory<SystemLogConfig> systemLogBeanDefinitionFactory() {
        return new SystemLogBeanDefinitionFactory();
    }

    @Bean
    public BeanDefinitionFactory<LogConfig> accessLogBeanDefinitionFactory() {
        return new AccessLogBeanDefinitionFactory();
    }

    @Bean
    public Config jsonConfig() {
        return JSON_CONFIG;
    }

    @Bean
    public Host defaultHost() {
        return new Host(JSON_CONFIG.getHost(), JSON_CONFIG.getPort());
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
    public DucksboardPoster ducksboardPoster() {
        return new DucksboardPoster(ducksboardApiKey(), restTemplate());
    }
    */

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

    /*@Bean
    @Scope("prototype")
    public FrontendHttpHandler frontendHttpHandler() {
        return new FrontendHttpHandler();
    }*/

    @Bean(name = "virtualHosts")
    public Map<String, VirtualHostConfig> virtualHosts() {
        List<VirtualHostConfig> configs = JSON_CONFIG.getVhosts();
        Map<String, VirtualHostConfig> vhosts = new LinkedHashMap<>(configs.size());
        for (VirtualHostConfig config : configs) {
            vhosts.put(config.getName().toLowerCase(), config);
        }

        return vhosts;
    }

    public VirtualHostConfig vhostConfig() {
        return JSON_CONFIG.getVhosts().iterator().next();
    }

    @Bean
    public KeyEntryFactory keyEntryFactory() {
        return new BouncyCastleKeyEntryFactory();
    }

    @Bean
    public KeyStore keyStore() throws Exception {

        String name = jsonConfig().getName();

        Random r = new SecureRandom();
        byte[] bytes = new byte[32];
        r.nextBytes(bytes);
        byte[] base64Encoded = Base64.getEncoder().encode(bytes);
        char[] randomPassword = new char[base64Encoded.length];
        for (int i = base64Encoded.length; i-- > 0; ) {
            randomPassword[i] = (char) (base64Encoded[i] & 0xff);
        }

        char[] password = "changeit".toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null);

        KeyEntryFactory keyEntryFactory = keyEntryFactory();

        //now load the keystore w/ the keys referenced in configuration:

        //first, load the system-wide default:
        TlsConfig tlsConfig = jsonConfig().getTls();
        if (tlsConfig != null) {
            KeyEntry keyEntry = keyEntryFactory.createKeyEntry(name, tlsConfig);
            ks.setKeyEntry(name, keyEntry.getPrivateKey(), password, keyEntry.getCertificateChain());
        }

        //then load vhosts:
        Map<String, VirtualHostConfig> vhosts = virtualHosts();
        for (Map.Entry<String, VirtualHostConfig> entry : vhosts.entrySet()) {
            VirtualHostConfig vhost = entry.getValue();
            tlsConfig = vhost.getTls();
            if (tlsConfig != null) {
                String vhostName = vhost.getName();
                KeyEntry keyEntry = keyEntryFactory.createKeyEntry(vhostName, tlsConfig);
                ks.setKeyEntry(vhostName, keyEntry.getPrivateKey(), password, keyEntry.getCertificateChain());
            }
        }

        return ks;
    }

    @Bean
    public SSLContext sslContext() throws Exception {

        KeyStore keyStore = keyStore();

        KeyManagerFactory factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        factory.init(keyStore, "changeit".toCharArray());

        // Javadoc of SSLContext.init() states the first KeyManager implementing X509ExtendedKeyManager in the array is
        // used. We duplicate this behaviour when picking the KeyManager to wrap around.
        X509ExtendedKeyManager x509KeyManager = null;
        for (KeyManager keyManager : factory.getKeyManagers()) {
            if (keyManager instanceof X509ExtendedKeyManager) {
                x509KeyManager = (X509ExtendedKeyManager) keyManager;
            }
        }

        if (x509KeyManager == null) {
            throw new Exception("KeyManagerFactory did not create an X509ExtendedKeyManager");
        }

        SniKeyManager sniKeyManager = new SniKeyManager(x509KeyManager);

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(new KeyManager[]{sniKeyManager}, null, null);

        return context;
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

    @Bean
    public StormpathConfig stormpathConfig() {
        return vhostConfig().getStormpath();
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
