package io.darkstar.plugin.http;

import io.darkstar.config.ContextAttribute;
import io.darkstar.config.DefaultContextAttribute;
import io.darkstar.config.http.HttpContext;
import io.darkstar.config.http.VirtualHost;
import io.darkstar.plugin.AbstractPlugin;
import io.darkstar.plugin.Directive;
import io.darkstar.plugin.Directives;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
//@Plugin
public class VirtualHostsPlugin extends AbstractPlugin {

    public static final Map<String, Directive> DIRECTIVES = Directives.builder().add("vhosts", HttpContext.class).buildMap();

    @Autowired
    private VirtualHostPlugin vhostPlugin;

    @Override
    public Map<String, Directive> getDirectives() {
        return DIRECTIVES;
    }

    @Override
    protected Object onHttpAttribute(ContextAttribute<HttpContext> attribute) {
        Map<String, Map> vhostDefinitions = (Map<String, Map>) attribute.getValue();

        Map<String, VirtualHost> converted = new LinkedHashMap<>();

        //convert the definitions into VirtualHost objects
        for (Map.Entry<String, Map> entry : vhostDefinitions.entrySet()) {

            String vhostName = entry.getKey();
            Map vhostAttributes = entry.getValue();

            ContextAttribute<HttpContext> attr =
                    new DefaultContextAttribute<>(vhostName, vhostAttributes, attribute.getContext());

            VirtualHost vhost = (VirtualHost) vhostPlugin.onConfigAttribute(attr);

            converted.put(vhostName, vhost);
        }

        return converted;
    }
}
