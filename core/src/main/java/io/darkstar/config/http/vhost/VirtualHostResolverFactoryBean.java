package io.darkstar.config.http.vhost;

import io.darkstar.http.DefaultVirtualHostResolver;
import io.darkstar.http.VirtualHostResolver;
import io.darkstar.http.VirtualHostStore;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.util.Assert;

public class VirtualHostResolverFactoryBean extends AbstractFactoryBean {

    private VirtualHostStore virtualHostStore;

    public void setVirtualHostStore(VirtualHostStore virtualHostStore) {
        Assert.notNull(virtualHostStore, "virtualHostStore cannot be null.");
        this.virtualHostStore = virtualHostStore;
    }

    @Override
    public Class<?> getObjectType() {
        return VirtualHostResolver.class;
    }

    @Override
    protected Object createInstance() throws Exception {
        return this.virtualHostStore != null ?
                new DefaultVirtualHostResolver(this.virtualHostStore) :
                new DefaultVirtualHostResolver();
    }
}
