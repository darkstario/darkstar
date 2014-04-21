package io.darkstar.plugin.stormpath;

import io.darkstar.config.http.VirtualHost;
import io.darkstar.plugin.AbstractPlugin;
import io.darkstar.plugin.stereotype.Plugin;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Plugin
public class StormpathPlugin extends AbstractPlugin {

    private static final Set<String> NAMES = new HashSet<>(Arrays.asList("stormpath"));


    @Override
    public Set<String> getSupportedAttributeNames() {
        return NAMES;
    }

    @Override
    protected Object onVirtualHostConfigAttribute(String attributeName, Object configValue, VirtualHost applicableContext) {
        return configValue;
    }
}
