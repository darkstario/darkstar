package io.darkstar.config.http;

import com.stormpath.sdk.lang.Assert;
import io.darkstar.config.DefaultContext;
import io.darkstar.config.SystemContext;

import java.util.Map;

public class DefaultHttpContext extends DefaultContext<SystemContext> implements HttpContext {

    public static final String NAME = "http";

    public DefaultHttpContext(SystemContext parent) {
        super(NAME, parent);
        Assert.notNull(parent, "parent context is required.");
    }

    @Override
    public VirtualHost getVirtualHost(String name) {
        return getVirtualHosts().get(name);
    }

    private Map<String,VirtualHost> getVirtualHosts() {
        return getAttribute("vhosts");
    }

    @Override
    public Cluster getCluster(String name) {
        return getClusters().get(name);
    }

    private Map<String,Cluster> getClusters() {
        return getAttribute("clusters");
    }
}
