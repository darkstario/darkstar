package io.darkstar.plugin.http;

import io.darkstar.config.IdentifierName;
import io.darkstar.config.SystemContext;
import io.darkstar.config.http.Cluster;
import io.darkstar.config.http.HttpContext;
import io.darkstar.config.http.Route;
import io.darkstar.config.http.VirtualHost;
import io.darkstar.plugin.AbstractPlugin;
import io.darkstar.plugin.stereotype.Plugin;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Plugin
public class ClustersPlugin extends AbstractPlugin {

    private static final Set<String> NAMES = IdentifierName.setOf("clusters");

    @Autowired
    private ClusterPlugin clusterPlugin;

    @Override
    public Set<String> getDirectiveNames() {
        return NAMES;
    }

    @Override
    protected Object onSystemDirective(String directiveName, Object directiveValue, SystemContext ctx) {
        throw new UnsupportedOperationException("Clusters may not be defined in the system context.");
    }

    @Override
    protected Object onRouteDirective(String directiveName, Object directiveValue, Route route) {
        throw new UnsupportedOperationException("Clusters may not be defined in a route. (route: " + route + ")");
    }

    @Override
    protected Object onHttpDirective(String directiveName, Object directiveValue, HttpContext ctx) {

        Map<String, Map> clusterDefinitions = (Map<String, Map>) directiveValue;

        Map<String, Cluster> converted = new LinkedHashMap<>();

        //convert the definitions into VirtualHost objects
        for (Map.Entry<String, Map> entry : clusterDefinitions.entrySet()) {

            String clusterName = entry.getKey();
            Map clusterAttributes = entry.getValue();

            Cluster cluster = (Cluster) clusterPlugin.onHttpDirective(clusterName, clusterAttributes, ctx);

            converted.put(clusterName, cluster);
        }

        return converted;
    }

    @Override
    protected Object onVirtualHostDirective(String directiveName, Object directiveValue, VirtualHost vhost) {

        Map<String, Map> clusterDefinitions = (Map<String, Map>) directiveValue;

        Map<String, Cluster> converted = new LinkedHashMap<>();

        //convert the definitions into VirtualHost objects
        for (Map.Entry<String, Map> entry : clusterDefinitions.entrySet()) {

            String clusterName = entry.getKey();
            Map clusterAttributes = entry.getValue();

            Cluster cluster = (Cluster) clusterPlugin.onVirtualHostDirective(clusterName, clusterAttributes, vhost);

            converted.put(clusterName, cluster);
        }

        return converted;
    }
}
