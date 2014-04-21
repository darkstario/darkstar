package io.darkstar.config.http;

import io.darkstar.config.Context;

public interface VirtualHost extends Context<HttpContext> {

    Cluster getCluster(String name);
}
