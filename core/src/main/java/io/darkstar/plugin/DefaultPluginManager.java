package io.darkstar.plugin;

import io.darkstar.config.IdentifierName;
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

    @Autowired
    private Collection<Plugin> plugins;

    private final Map<String, Plugin> registeredPlugins;

    public DefaultPluginManager() {
        registeredPlugins = new HashMap<>();
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        for (Plugin plugin : plugins) {

            Set<String> directiveNames = plugin.getDirectiveNames();
            if (CollectionUtils.isEmpty(directiveNames)) {
                continue;
            }

            for(final String directiveName : directiveNames) {

                String canonicalDirectiveName = IdentifierName.of(directiveName);

                Plugin existing = registeredPlugins.get(canonicalDirectiveName);

                if (existing != null) {
                    String msg = "Plugin registration collision: The " + plugin.getName() + " plugin claims to " +
                            "support '" + directiveName + "' directives, but the " + existing.getName() + " plugin " +
                            "has already been registered to support this directive name.  You must remove one of " +
                            "these plugins to avoid this conflict.";
                    throw new BeanInitializationException(msg);
                }

                registeredPlugins.put(canonicalDirectiveName, plugin);
            }
        }

        log.debug("Registered {} plugins.", registeredPlugins.size());
    }

    @Override
    public Plugin getPluginForDirective(String directiveName) {
        String canonicalDirectiveName = IdentifierName.of(directiveName);
        return this.registeredPlugins.get(canonicalDirectiveName);
    }
}
