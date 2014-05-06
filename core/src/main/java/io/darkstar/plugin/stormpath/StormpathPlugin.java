package io.darkstar.plugin.stormpath;

import io.darkstar.config.ContextAttribute;
import io.darkstar.config.http.VirtualHost;
import io.darkstar.plugin.AbstractPlugin;
import io.darkstar.plugin.Directive;
import io.darkstar.plugin.Directives;

import java.util.Map;

//@Plugin
public class StormpathPlugin extends AbstractPlugin {

    public static final Map<String, Directive> DIRECTIVES = Directives.builder()
            .add("stormpath", VirtualHost.class).buildMap();

    @Override
    public Map<String, Directive> getDirectives() {
        return DIRECTIVES;
    }

    @Override
    protected Object onVirtualHostAttribute(ContextAttribute<VirtualHost> attribute) {
        return attribute.getValue(); //TODO: implement
    }
}
