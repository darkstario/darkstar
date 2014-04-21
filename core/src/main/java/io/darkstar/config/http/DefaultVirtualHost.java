package io.darkstar.config.http;

import com.stormpath.sdk.lang.Assert;
import io.darkstar.config.Cluster;
import io.darkstar.config.DefaultContext;

import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultVirtualHost extends DefaultContext<HttpContext> implements VirtualHost {

    private final Map<String, Cluster> clusters;

    public DefaultVirtualHost(String name, HttpContext parent) {
        super(name, parent);
        Assert.notNull(parent, "parent context is required.");
        this.clusters = new LinkedHashMap<>();
    }

    @Override
    public Cluster getCluster(String name) {
        return this.clusters.get(name);
    }
}
