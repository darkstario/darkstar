package io.darkstar.plugin.http;

import io.darkstar.config.ContextAttribute;
import io.darkstar.config.DefaultContextAttribute;
import io.darkstar.config.http.Cluster;
import io.darkstar.config.http.HttpContext;
import io.darkstar.config.http.VirtualHost;
import io.darkstar.plugin.AbstractPlugin;
import io.darkstar.plugin.Directive;
import io.darkstar.plugin.Directives;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.Map;

//@Plugin
public class ClustersPlugin extends AbstractPlugin {

    private static final Map<String, Directive> DIRECTIVES = Directives.builder()
            .add("clusters", Directives.setOf(HttpContext.class, VirtualHost.class)).buildMap();

    @Autowired
    private ClusterPlugin clusterPlugin;

    @Override
    public Map<String, Directive> getDirectives() {
        return DIRECTIVES;
    }

    @Override
    protected Object onHttpAttribute(ContextAttribute<HttpContext> attribute) {
        return createClusters(attribute);
    }

    @Override
    protected Object onVirtualHostAttribute(ContextAttribute<VirtualHost> attribute) {
        return createClusters(attribute);
    }

    protected Object createClusters(ContextAttribute attribute) {

        Map<String, Map> clusterDefinitions = (Map<String, Map>) attribute.getValue();

        Map<String, Cluster> converted = new LinkedHashMap<>();

        //convert the definitions into VirtualHost objects
        for (Map.Entry<String, Map> entry : clusterDefinitions.entrySet()) {

            String clusterName = entry.getKey();
            Map clusterAttributes = entry.getValue();

            ContextAttribute attr = new DefaultContextAttribute<>(clusterName, clusterAttributes, attribute.getContext());

            Cluster cluster = (Cluster) clusterPlugin.onConfigAttribute(attr);

            converted.put(clusterName, cluster);
        }

        return converted;
    }
}
