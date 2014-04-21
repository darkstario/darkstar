package io.darkstar.plugin.stormpath;

import io.darkstar.config.IdentifierName;
import io.darkstar.config.http.VirtualHost;
import io.darkstar.plugin.AbstractPlugin;
import io.darkstar.plugin.stereotype.Plugin;

import java.util.Set;

@Plugin
public class StormpathPlugin extends AbstractPlugin {

    private static final Set<String> NAMES = IdentifierName.setOf("stormpath");

    @Override
    public Set<String> getDirectiveNames() {
        return NAMES;
    }

    @Override
    protected Object onVirtualHostDirective(String directiveName, Object directiveValue, VirtualHost vhost) {
        return directiveValue;
    }
}
