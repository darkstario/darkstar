package io.darkstar.plugin.http;

import io.darkstar.config.http.DefaultVirtualHost;
import io.darkstar.config.http.HttpContext;
import io.darkstar.plugin.AbstractPlugin;
import io.darkstar.plugin.stereotype.Plugin;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unchecked")
@Plugin
public class VirtualHostPlugin extends AbstractPlugin {

    @Override
    public Set<String> getDirectiveNames() {
        return Collections.emptySet();
    }

    @Override
    protected Object onHttpDirective(String vhostName, Object vhostConfigAttributes, HttpContext ctx) {

        Map<String,Object> attributes = (Map<String,Object>)vhostConfigAttributes;

        DefaultVirtualHost vhost = new DefaultVirtualHost(vhostName, ctx);

        vhost.setAttributes(attributes);

        return vhost;
    }
}
