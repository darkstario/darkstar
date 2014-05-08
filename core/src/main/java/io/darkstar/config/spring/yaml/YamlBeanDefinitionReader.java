package io.darkstar.config.spring.yaml;

import io.darkstar.config.yaml.DefaultNodeFactory;
import io.darkstar.config.yaml.Node;
import io.darkstar.config.yaml.NodeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(YamlBeanDefinitionReader.class);

    private Class<? extends BeanDefinitionDocumentReader> contentReaderClass = DefaultBeanDefinitionDocumentReader.class;

    private NodeFactory nodeFactory = new DefaultNodeFactory();

    private ProblemReporter problemReporter = new FailFastProblemReporter();

    private ReaderEventListener eventListener = new EmptyReaderEventListener();

    private SourceExtractor sourceExtractor = new NullSourceExtractor();

    private BeanDefinitionParserResolver beanDefinitionParserResolver;

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
     * Specify the {@link BeanDefinitionDocumentReader} implementation to use,
     * responsible for the actual reading of the YAML content document.
     * <p>The default is {@link DefaultBeanDefinitionDocumentReader}.
     *
     * @param contentReaderClass the desired BeanDefinitionDocumentReader implementation class
     */
    public void setContentReaderClass(Class<? extends BeanDefinitionDocumentReader> contentReaderClass) {
        Assert.isTrue(contentReaderClass != null && BeanDefinitionDocumentReader.class.isAssignableFrom(contentReaderClass),
                "contentReaderClass must be an implementation of the BeanDefinitionDocumentReader interface");
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

        log.debug("Loading YAML bean definitions from {}", encodedResource);

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
            Node root = nodeFactory.createGraph("main", content);
            return registerBeanDefinitions(root, resource);
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
     * @see BeanDefinitionDocumentReader#registerBeanDefinitions
     */
    public int registerBeanDefinitions(Node content, Resource resource) throws BeanDefinitionStoreException {
        BeanDefinitionDocumentReader documentReader = createBeanDefinitionContentReader();
        documentReader.setEnvironment(this.getEnvironment());
        int countBefore = getRegistry().getBeanDefinitionCount();
        documentReader.registerBeanDefinitions(content, createReaderContext(resource));
        return getRegistry().getBeanDefinitionCount() - countBefore;
    }

    /**
     * Create the {@link BeanDefinitionDocumentReader} to use for actually reading bean definitions from a YAML document.
     * <p>The default implementation instantiates the specified
     * {@link #setContentReaderClass(Class) contentReaderClass}</p>
     *
     * @return new BeanDefinitionDocumentReader instance.
     * @see #setContentReaderClass(Class)
     */
    protected BeanDefinitionDocumentReader createBeanDefinitionContentReader() {
        return BeanUtils.instantiateClass(this.contentReaderClass);
    }

    /**
     * Create the {@link YamlReaderContext} to pass over to the yaml reader.
     *
     * @param resource the resource for which a context will be created.
     * @return the {@link YamlReaderContext} to pass over to the yaml reader.
     */
    public YamlReaderContext createReaderContext(Resource resource) {
        return new YamlReaderContext(resource, this.problemReporter, this.eventListener,
                this.sourceExtractor, this, getEnvironment(), getBeanDefinitionParserResolver());
    }

    public BeanDefinitionParserResolver getBeanDefinitionParserResolver() {
        if (this.beanDefinitionParserResolver == null) {
            this.beanDefinitionParserResolver = createDefaultBeanDefinitionParserResolver();
        }
        return this.beanDefinitionParserResolver;
    }

    private BeanDefinitionParserResolver createDefaultBeanDefinitionParserResolver() {
        return new DefaultBeanDefinitionParserResolver(getResourceLoader().getClassLoader());
    }
}
