package io.darkstar.config.http;

import com.stormpath.sdk.lang.Assert;
import io.darkstar.config.Context;
import io.darkstar.config.DefaultContext;
import io.darkstar.config.SystemContext;

@SuppressWarnings("unchecked")
public class DefaultCluster extends DefaultContext implements Cluster {

    public DefaultCluster(String name, Context parent) {
        super(name, parent);
        Assert.notNull(parent, "parent context is required.");
        Assert.isTrue(!(parent instanceof SystemContext), "Cluster contexts cannot be used in the System context.");
    }
}
