package io.darkstar.config.http;

import io.darkstar.config.Cluster;
import io.darkstar.config.Context;
import io.darkstar.config.SystemContext;

public interface HttpContext extends Context<SystemContext> {

    VirtualHost getVirtualHost(String name);

    Cluster getCluster(String name);
}
