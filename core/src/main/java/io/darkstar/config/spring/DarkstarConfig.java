package io.darkstar.config.spring;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.FileAppender;
import com.codahale.metrics.MetricRegistry;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import io.darkstar.config.DefaultHostFactory;
import io.darkstar.config.Host;
import io.darkstar.config.HostFactory;
import io.darkstar.config.json.Config;
import io.darkstar.config.json.StormpathConfig;
import io.darkstar.config.json.TlsConfig;
import io.darkstar.config.json.VirtualHostConfig;
import io.darkstar.ducksboard.DucksboardPoster;
import io.darkstar.tls.BouncyCastleKeyEntryFactory;
import io.darkstar.tls.KeyEntry;
import io.darkstar.tls.KeyEntryFactory;
import io.darkstar.tls.SniKeyManager;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.cache.Caches;
import com.stormpath.sdk.client.Client;
import com.stormpath.sdk.client.Clients;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestTemplate;

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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Configuration
public class DarkstarConfig implements BeanDefinitionRegistryPostProcessor {

    public static Config JSON_CONFIG;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

        Map<String,String> logFormats = JSON_CONFIG.getLogFormat();
        for(Map.Entry<String,String> entry : logFormats.entrySet()) {
            String name = entry.getKey();
            String patternString = entry.getValue();

            GenericBeanDefinition def = new GenericBeanDefinition();
            def.setBeanClass(PatternLayoutEncoder.class);
            def.setInitMethodName("start");
            def.setDestroyMethodName("stop");
            def.setPropertyValues(new MutablePropertyValues()
                    .add("context", LoggerFactory.getILoggerFactory())
                    .add("pattern", patternString)
            );

            String beanName = name + "LogFormat" + PatternLayoutEncoder.class.getSimpleName();
            registry.registerBeanDefinition(beanName, def);
        }

        Map<String,VirtualHostConfig> vhosts = virtualHosts();

        for(VirtualHostConfig vhost : vhosts.values()) {

            String vhostName = vhost.getName();

            Map<String,Object> vhostLogs = vhost.getLog();

            for(Map.Entry<String,Object> log : vhostLogs.entrySet()) {
                String name = log.getKey();
                Object value = log.getValue();

                if (value instanceof String) {
                    String path = (String)value;

                    String encoderBeanName = name + "LogFormat" + PatternLayoutEncoder.class.getSimpleName();

                    GenericBeanDefinition def = new GenericBeanDefinition();
                    def.setBeanClass(FileAppender.class);
                    def.setInitMethodName("start");
                    def.setDestroyMethodName("stop");
                    def.setPropertyValues(new MutablePropertyValues()
                            .add("context", LoggerFactory.getILoggerFactory())
                            .add("file", path)
                            .add("encoder", new RuntimeBeanReference(encoderBeanName))
                    );

                    String beanName = vhostName + name + "log";

                    registry.registerBeanDefinition(beanName, def);
                }
            }
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        //no-op
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
        for (int i = base64Encoded.length; i-- > 0;) {
            randomPassword[i] = (char)(base64Encoded[i] & 0xff);
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
        Map<String,VirtualHostConfig> vhosts = virtualHosts();
        for(Map.Entry<String,VirtualHostConfig> entry : vhosts.entrySet()) {
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

    private String ducksboardApiKey() {
        return vhostConfig().getDucksboard().getApiKey();
    }

    private String datadogApiKey() {
        return vhostConfig().getDatadog().getApiKey();
    }

}
