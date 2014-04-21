package io.darkstar.config;

import io.darkstar.Darkstar;
import io.darkstar.plugin.Plugin;
import io.darkstar.plugin.PluginManager;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
@Component
public class ConfigHandler implements InitializingBean {

    private static final Map<String, Object> YAML = Collections.unmodifiableMap(Darkstar.YAML);

    @Autowired
    private PluginManager pluginManager;

    private Map<String, Object> effectiveConfig;

    public ConfigHandler() {
        this.effectiveConfig = new LinkedHashMap<>(YAML.size());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        DefaultSystemContext systemContext = new DefaultSystemContext();
        recurse(YAML, effectiveConfig, systemContext);
    }

    private void recurse(final Map<String, Object> config, final Map<String, Object> effectiveConfig, Context context) {

        for (Map.Entry<String, Object> entry : config.entrySet()) {

            final String directiveName = entry.getKey();
            final Object value = entry.getValue();

            boolean complex = value instanceof Map || value instanceof Collection;

            Plugin plugin = pluginManager.getPluginForDirective(directiveName);

            Object effectiveValue;

            if (plugin != null) {
                effectiveValue = plugin.onConfigDirective(directiveName, value, context);
            } else {
                if (complex) {
                    //plugins must process complex attributes:
                    String msg = "The '" + directiveName + "' directive is not supported in " +
                            context.getName() + " context configuration by any registered plugins.";
                    throw new BeanInitializationException(msg);
                }
                effectiveValue = value;
            }


            effectiveConfig.put(directiveName, effectiveValue);

            if (effectiveValue instanceof Context) {
                //We have a new Context! Yippee!

                DefaultContext child = (DefaultContext)effectiveValue;

                //all contexts must be a map of directives:
                Map<String, Object> originalAttributes = (Map<String, Object>) value;
                Map<String, Object> contextAttributes = child.getAttributes();

                recurse(originalAttributes, contextAttributes, child);
            }
        }
    }
}
