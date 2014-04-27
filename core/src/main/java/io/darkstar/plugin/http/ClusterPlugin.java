package io.darkstar.plugin.http;

import io.darkstar.config.ContextAttribute;
import io.darkstar.config.http.Cluster;
import io.darkstar.config.http.DefaultCluster;
import io.darkstar.config.http.HttpContext;
import io.darkstar.config.http.VirtualHost;
import io.darkstar.plugin.AbstractPlugin;
import io.darkstar.plugin.Directive;
import io.darkstar.plugin.stereotype.Plugin;

import java.util.Collections;
import java.util.Map;

@SuppressWarnings("unchecked")
@Plugin
public class ClusterPlugin extends AbstractPlugin {

    @Override
    public Map<String, Directive> getDirectives() {
        return Collections.emptyMap();
    }

    @Override
    protected Object onHttpAttribute(ContextAttribute<HttpContext> attribute) {
        return createCluster(attribute);
    }

    @Override
    protected Object onVirtualHostAttribute(ContextAttribute<VirtualHost> attribute) {
        return createCluster(attribute);
    }

    protected Cluster createCluster(ContextAttribute attribute) {
        Map<String, Object> attributes = (Map<String, Object>) attribute.getValue();
        DefaultCluster cluster = new DefaultCluster(attribute.getName(), attribute.getContext());
        cluster.setAttributes(attributes);
        return cluster;
    }
}
