package io.darkstar.config.http.vhost;

import io.darkstar.http.DefaultVirtualHostResolver;
import io.darkstar.http.VirtualHostResolver;
import org.springframework.beans.factory.config.AbstractFactoryBean;

public class VirtualHostResolverFactoryBean extends AbstractFactoryBean {

    @Override
    public Class<?> getObjectType() {
        return VirtualHostResolver.class;
    }

    @Override
    protected Object createInstance() throws Exception {
        return new DefaultVirtualHostResolver();
    }
}
