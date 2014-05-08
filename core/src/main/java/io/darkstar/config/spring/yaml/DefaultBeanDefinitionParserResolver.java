package io.darkstar.config.spring.yaml;

import io.darkstar.config.yaml.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.FatalBeanException;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultBeanDefinitionParserResolver implements BeanDefinitionParserResolver {

    /**
     * The location to look for the mapping files. Can be present in multiple JAR files.
     */
    public static final String DEFAULT_HANDLER_MAPPINGS_LOCATION = "META-INF/darkstar.yaml.parsers";

    /**
     * Logger available to subclasses
     */
    protected final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * ClassLoader to use for BeanDefinitionParser classes
     */
    private final ClassLoader classLoader;

    /**
     * Resource location to search for
     */
    private final String parserMappingsLocation;

    /**
     * Stores the mappings from namespace URI to BeanDefinitionParser class name / instance
     */
    private volatile Map<String, Object> parserMappings;


    /**
     * Create a new {@code DefaultBeanDefinitionParserResolver} using the default mapping file location.
     * <p>This constructor will result in the thread context ClassLoader being used to load resources.
     *
     * @see #DEFAULT_HANDLER_MAPPINGS_LOCATION
     */
    public DefaultBeanDefinitionParserResolver() {
        this(null, DEFAULT_HANDLER_MAPPINGS_LOCATION);
    }

    /**
     * Create a new {@code DefaultBeanDefinitionParserResolver} using the default mapping file location.
     *
     * @param classLoader the {@link ClassLoader} instance used to load mapping resources
     *                    (may be {@code null}, in which case the thread context ClassLoader will be used)
     * @see #DEFAULT_HANDLER_MAPPINGS_LOCATION
     */
    public DefaultBeanDefinitionParserResolver(ClassLoader classLoader) {
        this(classLoader, DEFAULT_HANDLER_MAPPINGS_LOCATION);
    }

    /**
     * Create a new {@code DefaultBeanDefinitionParserResolver} using the
     * supplied mapping file location.
     *
     * @param classLoader            the {@link ClassLoader} instance used to load mapping resources
     *                               may be {@code null}, in which case the thread context ClassLoader will be used)
     * @param parserMappingsLocation the mapping file location
     */
    public DefaultBeanDefinitionParserResolver(ClassLoader classLoader, String parserMappingsLocation) {
        Assert.notNull(parserMappingsLocation, "Handler mappings location must not be null");
        this.classLoader = (classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader());
        this.parserMappingsLocation = parserMappingsLocation;
    }


    /**
     * Locate the {@link BeanDefinitionParser} for the supplied namespace URI
     * from the configured mappings.
     *
     * @param node the relevant node
     * @return the located {@link BeanDefinitionParser}, or {@code null} if none found
     */
    @Override
    public BeanDefinitionParser resolveParser(Node node) {

        String nodeName = node.getName();
        Map<String, Object> handlerMappings = getParserMappings();
        Object handlerOrClassName = handlerMappings.get(nodeName);

        if (handlerOrClassName == null) {
            return null;
        } else if (handlerOrClassName instanceof BeanDefinitionParser) {
            return (BeanDefinitionParser) handlerOrClassName;
        } else {
            String className = (String) handlerOrClassName;
            try {
                Class<?> handlerClass = ClassUtils.forName(className, this.classLoader);
                if (!BeanDefinitionParser.class.isAssignableFrom(handlerClass)) {
                    String msg = "Class [" + className + "] for node name [" + nodeName +
                            "] does not implement the [" + BeanDefinitionParser.class.getName() + "] interface";
                    throw new FatalBeanException(msg);
                }
                BeanDefinitionParser beanDefinitionParser = (BeanDefinitionParser) BeanUtils.instantiateClass(handlerClass);
                handlerMappings.put(nodeName, beanDefinitionParser);
                return beanDefinitionParser;
            } catch (ClassNotFoundException ex) {
                throw new FatalBeanException("BeanDefinitionParser class [" + className + "] for node name [" +
                        nodeName + "] not found", ex);
            } catch (LinkageError err) {
                throw new FatalBeanException("Invalid BeanDefinitionParser class [" + className + "] for node name [" +
                        nodeName + "]: problem with handler class file or dependent class", err);
            }
        }
    }

    /**
     * Load the specified BeanDefinitionParser mappings lazily.
     */
    private Map<String, Object> getParserMappings() {
        if (this.parserMappings == null) {
            synchronized (this) {
                if (this.parserMappings == null) {
                    try {
                        Properties mappings =
                                PropertiesLoaderUtils.loadAllProperties(this.parserMappingsLocation, this.classLoader);

                        log.debug("Loaded BeanDefinitionParser mappings: {}", mappings);

                        Map<String, Object> handlerMappings = new ConcurrentHashMap<>(mappings.size());
                        CollectionUtils.mergePropertiesIntoMap(mappings, handlerMappings);

                        this.parserMappings = handlerMappings;

                    } catch (IOException ex) {
                        String msg = "Unable to load BeanDefinitionParser mappings from location [" +
                                this.parserMappingsLocation + "]";
                        throw new IllegalStateException(msg, ex);
                    }
                }
            }
        }
        return this.parserMappings;
    }


    @Override
    public String toString() {
        return "BeanDefinitionParserResolver using mappings " + getParserMappings();
    }
}
