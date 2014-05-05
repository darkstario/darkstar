package io.darkstar.config.spring.yaml;

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

/**
 * Default implementation of the {@link NodeHandlerResolver} interface.  Resolves namespace URIs to implementation
 * classes based on the mappings contained in mapping file.
 * <p/>
 * <p>By default, this implementation looks for the mapping file at {@code META-INF/darkstar.yaml.handlers}, but this
 * can be changed using the {@link #DefaultNodeHandlerResolver(ClassLoader, String)} constructor.
 *
 * @see NodeHandler
 * @see DefaultBeanDefinitionDocumentReader
 */
public class DefaultNodeHandlerResolver implements NodeHandlerResolver {

    /**
     * The location to look for the mapping files. Can be present in multiple JAR files.
     */
    public static final String DEFAULT_HANDLER_MAPPINGS_LOCATION = "META-INF/darkstar.yaml.handlers";

    /**
     * Logger available to subclasses
     */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * ClassLoader to use for NodeHandler classes
     */
    private final ClassLoader classLoader;

    /**
     * Resource location to search for
     */
    private final String handlerMappingsLocation;

    /**
     * Stores the mappings from namespace URI to NodeHandler class name / instance
     */
    private volatile Map<String, Object> handlerMappings;


    /**
     * Create a new {@code DefaultNodeHandlerResolver} using the default mapping file location.
     * <p>This constructor will result in the thread context ClassLoader being used to load resources.
     *
     * @see #DEFAULT_HANDLER_MAPPINGS_LOCATION
     */
    public DefaultNodeHandlerResolver() {
        this(null, DEFAULT_HANDLER_MAPPINGS_LOCATION);
    }

    /**
     * Create a new {@code DefaultNodeHandlerResolver} using the default mapping file location.
     *
     * @param classLoader the {@link ClassLoader} instance used to load mapping resources
     *                    (may be {@code null}, in which case the thread context ClassLoader will be used)
     * @see #DEFAULT_HANDLER_MAPPINGS_LOCATION
     */
    public DefaultNodeHandlerResolver(ClassLoader classLoader) {
        this(classLoader, DEFAULT_HANDLER_MAPPINGS_LOCATION);
    }

    /**
     * Create a new {@code DefaultNodeHandlerResolver} using the
     * supplied mapping file location.
     *
     * @param classLoader             the {@link ClassLoader} instance used to load mapping resources
     *                                may be {@code null}, in which case the thread context ClassLoader will be used)
     * @param handlerMappingsLocation the mapping file location
     */
    public DefaultNodeHandlerResolver(ClassLoader classLoader, String handlerMappingsLocation) {
        Assert.notNull(handlerMappingsLocation, "Handler mappings location must not be null");
        this.classLoader = (classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader());
        this.handlerMappingsLocation = handlerMappingsLocation;
    }


    /**
     * Locate the {@link NodeHandler} for the supplied namespace URI
     * from the configured mappings.
     *
     * @param nodeName the relevant namespace URI
     * @return the located {@link NodeHandler}, or {@code null} if none found
     */
    @Override
    public NodeHandler resolve(String nodeName) {
        Map<String, Object> handlerMappings = getHandlerMappings();
        Object handlerOrClassName = handlerMappings.get(nodeName);
        if (handlerOrClassName == null) {
            return null;
        } else if (handlerOrClassName instanceof NodeHandler) {
            return (NodeHandler) handlerOrClassName;
        } else {
            String className = (String) handlerOrClassName;
            try {
                Class<?> handlerClass = ClassUtils.forName(className, this.classLoader);
                if (!NodeHandler.class.isAssignableFrom(handlerClass)) {
                    String msg = "Class [" + className + "] for node name [" + nodeName +
                            "] does not implement the [" + NodeHandler.class.getName() + "] interface";
                    throw new FatalBeanException(msg);
                }
                NodeHandler nodeHandler = (NodeHandler) BeanUtils.instantiateClass(handlerClass);
                nodeHandler.init();
                handlerMappings.put(nodeName, nodeHandler);
                return nodeHandler;
            } catch (ClassNotFoundException ex) {
                throw new FatalBeanException("NodeHandler class [" + className + "] for node name [" +
                        nodeName + "] not found", ex);
            } catch (LinkageError err) {
                throw new FatalBeanException("Invalid NodeHandler class [" + className + "] for node name [" +
                        nodeName + "]: problem with handler class file or dependent class", err);
            }
        }
    }

    /**
     * Load the specified NodeHandler mappings lazily.
     */
    private Map<String, Object> getHandlerMappings() {
        if (this.handlerMappings == null) {
            synchronized (this) {
                if (this.handlerMappings == null) {
                    try {
                        Properties mappings =
                                PropertiesLoaderUtils.loadAllProperties(this.handlerMappingsLocation, this.classLoader);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Loaded NodeHandler mappings: " + mappings);
                        }
                        Map<String, Object> handlerMappings = new ConcurrentHashMap<String, Object>(mappings.size());
                        CollectionUtils.mergePropertiesIntoMap(mappings, handlerMappings);
                        this.handlerMappings = handlerMappings;
                    } catch (IOException ex) {
                        throw new IllegalStateException(
                                "Unable to load NodeHandler mappings from location [" + this.handlerMappingsLocation + "]", ex);
                    }
                }
            }
        }
        return this.handlerMappings;
    }


    @Override
    public String toString() {
        return "NodeHandlerResolver using mappings " + getHandlerMappings();
    }


}
