package io.darkstar.config.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.config.AbstractFactoryBean;

public class HazelcastInstanceFactoryBean extends AbstractFactoryBean<HazelcastInstance> {

    @Override
    public Class<?> getObjectType() {
        return HazelcastInstance.class;
    }

    @Override
    protected HazelcastInstance createInstance() throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
