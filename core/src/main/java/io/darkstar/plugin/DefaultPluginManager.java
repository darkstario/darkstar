package io.darkstar.plugin;

import io.darkstar.config.ContextAttribute;
import io.darkstar.config.IdentifierName;
import io.darkstar.config.yaml.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class DefaultPluginManager implements PluginManager, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(DefaultPluginManager.class);

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Autowired
    private Collection<Plugin> plugins;

    private final Map<String, Plugin> registeredPlugins;

    public DefaultPluginManager() {
        registeredPlugins = new HashMap<>();
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        for (Plugin plugin : plugins) {

            Map<String,Directive> directives = plugin.getDirectives();

            if (CollectionUtils.isEmpty(directives)) {
                continue;
            }

            for(final Map.Entry<String,Directive> entry : directives.entrySet()) {

                String name = entry.getKey();

                String canonicalName = IdentifierName.of(name);

                Plugin existing = registeredPlugins.get(canonicalName);

                if (existing != null) {
                    String msg = "Plugin registration collision: The " + plugin.getName() + " plugin claims to " +
                            "support '" + name + "' directives, but the " + existing.getName() + " plugin " +
                            "has already been registered to support this directive name.  You must remove one of " +
                            "these plugins to avoid this conflict.";
                    throw new BeanInitializationException(msg);
                }

                registeredPlugins.put(canonicalName, plugin);
            }
        }

        log.debug("Registered {} plugins.", registeredPlugins.size());
    }

    @Override
    public Plugin getPlugin(ContextAttribute attribute) {
        String canonicalName = IdentifierName.of(attribute.getName());
        return this.registeredPlugins.get(canonicalName);
    }

    @Override
    public Plugin getPlugin(Node node) {
        String canonicalName = node.getName();
        return this.registeredPlugins.get(canonicalName);
    }
}
