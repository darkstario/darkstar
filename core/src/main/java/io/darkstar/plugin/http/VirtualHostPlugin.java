package io.darkstar.plugin.http;

import io.darkstar.config.ContextAttribute;
import io.darkstar.config.http.DefaultVirtualHost;
import io.darkstar.config.http.HttpContext;
import io.darkstar.plugin.AbstractPlugin;
import io.darkstar.plugin.Directive;
import io.darkstar.plugin.stereotype.Plugin;

import java.util.Collections;
import java.util.Map;

@SuppressWarnings("unchecked")
@Plugin
public class VirtualHostPlugin extends AbstractPlugin {

    @Override
    public Map<String, Directive> getDirectives() {
        return Collections.emptyMap();
    }

    @Override
    protected Object onHttpAttribute(ContextAttribute<HttpContext> attribute) {
        Map<String, Object> attributes = (Map<String, Object>) attribute.getValue();

        DefaultVirtualHost vhost = new DefaultVirtualHost(attribute.getName(), attribute.getContext());

        vhost.setAttributes(attributes);

        return vhost;
    }
}
