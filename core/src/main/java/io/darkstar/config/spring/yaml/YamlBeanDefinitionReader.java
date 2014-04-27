package io.darkstar.config.spring.yaml;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.parsing.EmptyReaderEventListener;
import org.springframework.beans.factory.parsing.FailFastProblemReporter;
import org.springframework.beans.factory.parsing.NullSourceExtractor;
import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.beans.factory.parsing.ReaderEventListener;
import org.springframework.beans.factory.parsing.SourceExtractor;
import org.springframework.beans.factory.support.AbstractBeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.util.Assert;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class YamlBeanDefinitionReader extends AbstractBeanDefinitionReader {

    private Class<? extends BeanDefinitionContentReader> contentReaderClass = DefaultBeanDefinitionContentReader.class;

    private ProblemReporter problemReporter = new FailFastProblemReporter();

    private ReaderEventListener eventListener = new EmptyReaderEventListener();

    private SourceExtractor sourceExtractor = new NullSourceExtractor();

    private final ThreadLocal<Set<EncodedResource>> resourcesCurrentlyBeingLoaded =
            new NamedThreadLocal<>("YAML bean definition resources currently being loaded");

    /**
     * Create new YamlBeanDefinitionReader for the given bean factory.
     *
     * @param registry the BeanFactory to load bean definitions into, in the form of a BeanDefinitionRegistry
     */
    public YamlBeanDefinitionReader(BeanDefinitionRegistry registry) {
        super(registry);
    }

    /**
     * Specify the {@link BeanDefinitionContentReader} implementation to use,
     * responsible for the actual reading of the YAML content document.
     * <p>The default is {@link DefaultBeanDefinitionContentReader}.
     *
     * @param contentReaderClass the desired BeanDefinitionContentReader implementation class
     */
    public void setContentReaderClass(Class<? extends BeanDefinitionContentReader> contentReaderClass) {
        Assert.isTrue(contentReaderClass != null && BeanDefinitionContentReader.class.isAssignableFrom(contentReaderClass),
                "contentReaderClass must be an implementation of the BeanDefinitionContentReader interface");
        this.contentReaderClass = contentReaderClass;
    }

    /**
     * Load bean definitions from the specified YAML file.
     *
     * @param resource the resource descriptor for the YAML file
     * @return the number of bean definitions found
     * @throws BeanDefinitionStoreException in case of loading or parsing errors
     */
    @Override
    public int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException {
        return loadBeanDefinitions(new EncodedResource(resource));
    }

    /**
     * Load bean definitions from the specified YAML file.
     *
     * @param encodedResource the resource descriptor for the YAML file,
     *                        allowing to specify an encoding to use for parsing the file
     * @return the number of bean definitions found
     * @throws BeanDefinitionStoreException in case of loading or parsing errors
     */
    public int loadBeanDefinitions(EncodedResource encodedResource) throws BeanDefinitionStoreException {
        Assert.notNull(encodedResource, "EncodedResource must not be null");
        if (logger.isInfoEnabled()) {
            logger.info("Loading YAML bean definitions from " + encodedResource.getResource());
        }

        Set<EncodedResource> currentResources = this.resourcesCurrentlyBeingLoaded.get();
        if (currentResources == null) {
            currentResources = new HashSet<>(4);
            this.resourcesCurrentlyBeingLoaded.set(currentResources);
        }
        if (!currentResources.add(encodedResource)) {
            throw new BeanDefinitionStoreException(
                    "Detected cyclic loading of " + encodedResource + " - check your import definitions!");
        }
        try {
            try (InputStream inputStream = encodedResource.getResource().getInputStream()) {
                Yaml yaml = createYamlInstance(inputStream, encodedResource);
                return doLoadBeanDefinitions(yaml, inputStream, encodedResource.getResource());
            }
        } catch (IOException ex) {
            throw new BeanDefinitionStoreException(
                    "IOException parsing YAML document from " + encodedResource.getResource(), ex);
        } finally {
            currentResources.remove(encodedResource);
            if (currentResources.isEmpty()) {
                this.resourcesCurrentlyBeingLoaded.remove();
            }
        }

    }

    @SuppressWarnings("UnusedParameters")
    protected Yaml createYamlInstance(InputStream is, EncodedResource resource) {
        return new Yaml();
    }

    @SuppressWarnings("UnusedParameters")
    protected Object loadYamlContent(Yaml yaml, InputStream is, Resource resource) {
        return yaml.load(is);
    }

    /**
     * Actually load bean definitions from the specified YAML file.
     *
     * @param yaml     the Yaml instance to use to load content
     * @param is       the InputStream to read
     * @param resource the resource descriptor for the YAML file
     * @return the number of bean definitions found
     * @throws BeanDefinitionStoreException in case of loading or parsing errors
     * @see #loadYamlContent
     * @see #registerBeanDefinitions
     */
    protected int doLoadBeanDefinitions(Yaml yaml, InputStream is, Resource resource) throws BeanDefinitionStoreException {
        try {
            Object content = loadYamlContent(yaml, is, resource);
            return registerBeanDefinitions(content, resource);
        } catch (BeanDefinitionStoreException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new BeanDefinitionStoreException(resource.getDescription(),
                    "Unexpected exception parsing Yaml document from " + resource, ex);
        }
    }

    /**
     * Register the bean definitions contained in the given YAML content. Called by {@link #loadBeanDefinitions}.
     * <p>Creates a new instance of the parser class and invokes {@code registerBeanDefinitions} on it.</p>
     *
     * @param content  the YAML document
     * @param resource the resource descriptor (for context information)
     * @return the number of bean definitions found
     * @throws BeanDefinitionStoreException in case of parsing errors
     * @see #loadBeanDefinitions
     * @see #setContentReaderClass(Class)
     * @see BeanDefinitionContentReader#registerBeanDefinitions
     */
    public int registerBeanDefinitions(Object content, Resource resource) throws BeanDefinitionStoreException {
        BeanDefinitionContentReader contentReader = createBeanDefinitionContentReader();
        contentReader.setEnvironment(this.getEnvironment());
        int countBefore = getRegistry().getBeanDefinitionCount();
        contentReader.registerBeanDefinitions(content, createReaderContext(resource));
        return getRegistry().getBeanDefinitionCount() - countBefore;
    }

    /**
     * Create the {@link BeanDefinitionContentReader} to use for actually reading bean definitions from a YAML document.
     * <p>The default implementation instantiates the specified
     * {@link #setContentReaderClass(Class) contentReaderClass}</p>
     *
     * @return new BeanDefinitionContentReader instance.
     * @see #setContentReaderClass(Class)
     */
    protected BeanDefinitionContentReader createBeanDefinitionContentReader() {
        return BeanUtils.instantiateClass(this.contentReaderClass);
    }

    /**
     * Create the {@link YamlReaderContext} to pass over to the yaml reader.
     *
     * @param resource the resource for which a context will be created.
     * @return the {@link YamlReaderContext} to pass over to the yaml reader.
     */
    public YamlReaderContext createReaderContext(Resource resource) {
        return new YamlReaderContext(resource, this.problemReporter, this.eventListener, this.sourceExtractor, this);
    }
}
