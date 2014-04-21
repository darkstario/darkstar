package io.darkstar.plugin.http;

import io.darkstar.config.http.DefaultVirtualHost;
import io.darkstar.config.http.HttpContext;
import io.darkstar.config.http.VirtualHost;
import io.darkstar.plugin.AbstractPlugin;
import io.darkstar.plugin.stereotype.Plugin;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unchecked")
@Plugin
public class VirtualHostsPlugin extends AbstractPlugin {

    private static final Set<String> NAMES = new HashSet<>(Arrays.asList("vhosts"));

    @Override
    public Set<String> getSupportedAttributeNames() {
        return NAMES;
    }

    @Override
    protected Object onHttpConfigAttribute(String attributeName, Object configValue, HttpContext ctx) {

        Map<String, Map> vhostDefinitions = (Map<String, Map>) configValue;

        Map<String, VirtualHost> converted = new LinkedHashMap<>();

        //convert the definitions into VirtualHost objects
        for (Map.Entry<String, Map> entry : vhostDefinitions.entrySet()) {
            String vhostName = entry.getKey();
            Map vhostAttributes = entry.getValue();
            DefaultVirtualHost vhost = new DefaultVirtualHost(vhostName, ctx);
            vhost.putAttributes(vhostAttributes);

            converted.put(vhostName, vhost);
        }

        return converted;
    }
}
