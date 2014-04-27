package io.darkstar.plugin;

import io.darkstar.config.ContextAttribute;

public interface PluginManager {

    Plugin getPlugin(ContextAttribute attribute);
}
