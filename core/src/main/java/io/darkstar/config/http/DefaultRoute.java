package io.darkstar.config.http;

import com.stormpath.sdk.lang.Assert;
import io.darkstar.config.Context;
import io.darkstar.config.DefaultContext;

public class DefaultRoute extends DefaultContext<VirtualHost> implements Context<VirtualHost> {

    public DefaultRoute(String name, VirtualHost parent) {
        super(name, parent);
        Assert.notNull(parent, "parent context is required.");
    }
}
