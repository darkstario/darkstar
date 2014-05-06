package io.darkstar.config;

import io.darkstar.Darkstar;
import io.darkstar.config.spring.yaml.YamlBeanDefinitionReader;
import io.darkstar.plugin.PluginManager;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

//@Component
public class ConfigHandler implements InitializingBean, DisposableBean {

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private ApplicationContext bootstrapAppCtx;

    private GenericApplicationContext configAppCtx;

    @Override
    public void afterPropertiesSet() throws Exception {
        GenericApplicationContext appCtx = new GenericApplicationContext(bootstrapAppCtx);
        appCtx.registerShutdownHook();
        YamlBeanDefinitionReader yamlReader = new YamlBeanDefinitionReader(appCtx);
        yamlReader.loadBeanDefinitions(Darkstar.YAML_FILE_PATH);
        appCtx.refresh();
        this.configAppCtx = appCtx;
    }

    @Override
    public void destroy() throws Exception {
        this.configAppCtx.destroy();
    }

    /*
    private Map<String,Object> recurse(Node config, Context context) {

        Map<String,Object> effectiveConfig = new LinkedHashMap<>(config.size());

        for (Map.Entry<String, Object> entry : config.entrySet()) {

            final String attributeName = entry.getKey();
            final String canonicalName = IdentifierName.of(attributeName);
            final Object value = entry.getValue();

            Node node;
            if (value instanceof Map) {
                Map<String,Object> kvPairs = (Map<String,Object>)value;
                node = new DefaultMappingNode(context, parent, canonicalName, kvPairs);
            } else if (value instanceof List) {
                List<Object> list = (List<Object>)value;
                node = new DefaultSequenceNode(context, parent, canonicalName, list);
            } else {
                node = new DefaultScalarNode(context, parent, canonicalName, value);
            }

            boolean complex = value instanceof Map || value instanceof Collection;

            Plugin plugin = pluginManager.getPlugin(node);

            Object effectiveValue;

            if (plugin != null) {
                effectiveValue = plugin.onConfigNode(node);
            } else {
                if (complex) {
                    //plugins must process complex attributes:
                    String msg = "The '" + attributeName + "' attribute is not supported in " +
                            context.getName() + " context configuration by any registered plugins.";
                    throw new BeanInitializationException(msg);
                }
                effectiveValue = value;
            }

            if (effectiveValue instanceof Context) {
                //We have a new Context! Yippee!

                DefaultContext child = (DefaultContext) effectiveValue;

                //all contexts must be a map of directives:
                Map<String, Object> originalAttributes = (Map<String, Object>) value;

                Map<String,Object> convertedAttributes = recurse(originalAttributes, node, child);

                child.setAttributes(convertedAttributes);

                effectiveValue = child;

            } else if (effectiveValue instanceof Map) {
                Map<String,Object> originalAttributes = (Map<String,Object>)effectiveValue;
                effectiveValue = recurse(originalAttributes, node, context);
            }

            effectiveConfig.put(attributeName, effectiveValue);

        }

        return effectiveConfig;
    }

    private void recurse(final Map<String, Object> config, final Map<String, Object> effectiveConfig, Context context) {

        for (Map.Entry<String, Object> entry : config.entrySet()) {

            final String attributeName = entry.getKey();
            final String canonicalName = IdentifierName.of(attributeName);
            final Object value = entry.getValue();


            final ContextAttribute attribute = new DefaultContextAttribute(canonicalName, value, context);

            boolean complex = value instanceof Map || value instanceof Collection;

            Plugin plugin = pluginManager.getPlugin(attribute);

            Object effectiveValue;

            if (plugin != null) {
                effectiveValue = plugin.onConfigAttribute(attribute);
            } else {
                if (complex) {
                    //plugins must process complex attributes:
                    String msg = "The '" + attributeName + "' attribute is not supported in " +
                            context.getName() + " context configuration by any registered plugins.";
                    throw new BeanInitializationException(msg);
                }
                effectiveValue = value;
            }

            effectiveConfig.put(attributeName, effectiveValue);

            if (effectiveValue instanceof Context) {
                //We have a new Context! Yippee!

                DefaultContext child = (DefaultContext) effectiveValue;

                //all contexts must be a map of directives:
                Map<String, Object> originalAttributes = (Map<String, Object>) value;
                Map<String, Object> contextAttributes = child.getAttributes();

                recurse(originalAttributes, contextAttributes, child);
            }
        }
    }
    */
}
