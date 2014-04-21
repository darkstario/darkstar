package io.darkstar.plugin.http;

import io.darkstar.config.Context;
import io.darkstar.config.SystemContext;
import io.darkstar.config.http.Cluster;
import io.darkstar.config.http.DefaultCluster;
import io.darkstar.config.http.HttpContext;
import io.darkstar.config.http.Route;
import io.darkstar.config.http.VirtualHost;
import io.darkstar.plugin.AbstractPlugin;
import io.darkstar.plugin.stereotype.Plugin;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unchecked")
@Plugin
public class ClusterPlugin extends AbstractPlugin {

    @Override
    public Set<String> getDirectiveNames() {
        return Collections.emptySet();
    }

    @Override
    protected Object onSystemDirective(String directiveName, Object directiveValue, SystemContext ctx) {
        throw new UnsupportedOperationException("Clusters may not be defined in the system context.");
    }

    @Override
    protected Object onHttpDirective(String clusterName, Object clusterAttributes, HttpContext ctx) {
        return createCluster(clusterName, clusterAttributes, ctx);
    }

    @Override
    protected Object onVirtualHostDirective(String clusterName, Object clusterAttributes, VirtualHost vhost) {
        return createCluster(clusterName, clusterAttributes, vhost);
    }

    @Override
    protected Object onRouteDirective(String directiveName, Object directiveValue, Route route) {
        throw new UnsupportedOperationException("Clusters may not be defined in a route. (route: " + route + ")");
    }

    protected Cluster createCluster(String clusterName, Object clusterAttributes, Context context) {
        Map<String,Object> attributes = (Map<String,Object>)clusterAttributes;
        DefaultCluster cluster = new DefaultCluster(clusterName, context);
        cluster.setAttributes(attributes);
        return cluster;
    }
}
