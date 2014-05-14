package io.darkstar.http;

import org.springframework.util.Assert;

public class DefaultVirtualHostResolver implements VirtualHostResolver {

    private final VirtualHostStore virtualHostStore;

    public DefaultVirtualHostResolver() {
        this(new DefaultVirtualHostStore());
    }

    public DefaultVirtualHostResolver(VirtualHostStore virtualHostStore) {
        Assert.notNull(virtualHostStore, "virtualHostStore cannot be null.");
        this.virtualHostStore = virtualHostStore;
    }

    @Override
    public VirtualHost getVirtualHost(String requestHostName) throws UnknownVirtualHostException {
        Assert.hasText(requestHostName, "requestHostName is required.");
        return this.virtualHostStore.getVirtualHost(requestHostName);
    }

}
