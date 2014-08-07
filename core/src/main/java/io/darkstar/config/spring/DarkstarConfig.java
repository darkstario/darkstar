package io.darkstar.config.spring;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.Environment;
import reactor.core.Reactor;
import reactor.core.spec.Reactors;

import java.util.concurrent.ExecutorService;

@SuppressWarnings("unchecked")
@Configuration
public class DarkstarConfig {

    @Bean(destroyMethod = "shutdown")
    public Environment reactorEnvironment() {
        return new Environment();
    }

    @Bean
    public Reactor reactor() {
        return Reactors.reactor().env(reactorEnvironment()).dispatcher(Environment.EVENT_LOOP).get();
    }

    @Bean
    public EventBus eventBus(ExecutorService executorService /* this is the Netty NioEventLoopGroup in config.spring.xml */) {
        return new AsyncEventBus(executorService);
    }
}
