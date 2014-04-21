package io.darkstar.plugin.http;

import io.darkstar.config.http.HttpContext;
import io.darkstar.config.http.VirtualHost;
import io.darkstar.plugin.AbstractPlugin;
import io.darkstar.plugin.stereotype.Plugin;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unchecked")
@Plugin
public class VirtualHostsPlugin extends AbstractPlugin {

    private static final Set<String> NAMES = new HashSet<>(Arrays.asList("vhosts"));

    @Autowired
    private VirtualHostPlugin vhostPlugin;

    @Override
    public Set<String> getDirectiveNames() {
        return NAMES;
    }

    @Override
    protected Object onHttpDirective(String directiveName, Object directiveValue, HttpContext ctx) {

        Map<String, Map> vhostDefinitions = (Map<String, Map>) directiveValue;

        Map<String, VirtualHost> converted = new LinkedHashMap<>();

        //convert the definitions into VirtualHost objects
        for (Map.Entry<String, Map> entry : vhostDefinitions.entrySet()) {

            String vhostName = entry.getKey();
            Map vhostAttributes = entry.getValue();

            VirtualHost vhost = (VirtualHost) vhostPlugin.onHttpDirective(vhostName, vhostAttributes, ctx);

            converted.put(vhostName, vhost);
        }

        return converted;
    }
}
