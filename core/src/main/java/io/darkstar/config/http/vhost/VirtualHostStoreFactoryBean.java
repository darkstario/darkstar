package io.darkstar.config.http.vhost;

import com.stormpath.sdk.lang.Assert;
import io.darkstar.http.DefaultVirtualHostStore;
import io.darkstar.http.VirtualHost;
import io.darkstar.http.VirtualHostStore;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.util.CollectionUtils;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class VirtualHostStoreFactoryBean extends AbstractFactoryBean<VirtualHostStore> {

    private ConcurrentMap<String, VirtualHost> sourceMap;

    private Set<VirtualHost> virtualHosts; //additions to the source map.

    public VirtualHostStoreFactoryBean() {
        this.sourceMap = new ConcurrentHashMap<>();
    }

    public void setSourceMap(ConcurrentMap<String, VirtualHost> sourceMap) {
        Assert.notNull(sourceMap, "sourceMap cannot be null.");
        this.sourceMap = sourceMap;
    }

    public void setVirtualHosts(Set<VirtualHost> virtualHosts) {
        Assert.notNull(virtualHosts, "virtualHosts cannot be null.");
        this.virtualHosts = virtualHosts;
    }

    @Override
    public Class<?> getObjectType() {
        return VirtualHostStore.class;
    }

    @Override
    protected VirtualHostStore createInstance() throws Exception {

        DefaultVirtualHostStore store = new DefaultVirtualHostStore(this.sourceMap);

        if (!CollectionUtils.isEmpty(this.virtualHosts)) {

            for (VirtualHost vhost : this.virtualHosts) {
                store.putIfAbsent(vhost);
            }
        }

        return store;
    }
}
