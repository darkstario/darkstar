package io.darkstar.plugin;

import io.darkstar.config.ContextAttribute;
import io.darkstar.config.yaml.Node;

public interface PluginManager {

    Plugin getPlugin(ContextAttribute attribute);

    Plugin getPlugin(Node node);
}
