package org.springframework.beans.factory.yaml;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractRefreshableConfigApplicationContext;
import org.springframework.core.io.Resource;

import java.io.IOException;

public class AbstractYamlApplicationContext extends AbstractRefreshableConfigApplicationContext {

    /**
     * Create a new AbstractYamlApplicationContext with no parent.
     */
    public AbstractYamlApplicationContext() {

    }

    /**
     * Create a new AbstractYamlApplicationContext with the given parent context.
     *
     * @param parent the parent context
     */
    public AbstractYamlApplicationContext(ApplicationContext parent) {
        super(parent);
    }

    /**
     * Loads the bean definitions via an YamlBeanDefinitionReader.
     *
     * @see YamlBeanDefinitionReader
     * @see #initBeanDefinitionReader
     * @see #loadBeanDefinitions
     */
    @Override
    protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
        YamlBeanDefinitionReader beanDefinitionReader = new YamlBeanDefinitionReader(beanFactory);

        // Configure the bean definition reader with this context's
        // resource loading environment.
        beanDefinitionReader.setEnvironment(this.getEnvironment());
        beanDefinitionReader.setResourceLoader(this);

        // Allow a subclass to provide custom initialization of the reader,
        // then proceed with actually loading the bean definitions.
        initBeanDefinitionReader(beanDefinitionReader);
        loadBeanDefinitions(beanDefinitionReader);
    }

    /**
     * Initialize the bean definition reader used for loading the bean
     * definitions of this context. Default implementation is empty, but can be overridden in subclasses for
     * custom initialization behavior.
     *
     * @param reader the bean definition reader used by this context
     * @see YamlBeanDefinitionReader#setContentReaderClass
     */
    @SuppressWarnings("UnusedParameters")
    protected void initBeanDefinitionReader(YamlBeanDefinitionReader reader) {
    }

    /**
     * Load the bean definitions with the given YamlBeanDefinitionReader.
     * <p>The lifecycle of the bean factory is handled by the {@link #refreshBeanFactory}
     * method; hence this method is just supposed to load and/or register bean definitions.
     *
     * @param reader the YamlBeanDefinitionReader to use
     * @throws BeansException in case of bean registration errors
     * @throws IOException    if the required YAML document isn't found
     * @see #refreshBeanFactory
     * @see #getConfigLocations
     * @see #getResources
     * @see #getResourcePatternResolver
     */
    protected void loadBeanDefinitions(YamlBeanDefinitionReader reader) throws BeansException, IOException {
        Resource[] configResources = getConfigResources();
        if (configResources != null) {
            reader.loadBeanDefinitions(configResources);
        }
        String[] configLocations = getConfigLocations();
        if (configLocations != null) {
            reader.loadBeanDefinitions(configLocations);
        }
    }

    /**
     * Return an array of Resource objects, referring to the YAML files that this context should be built with.
     * <p>The default implementation returns {@code null}. Subclasses can override
     * this to provide pre-built Resource objects rather than location Strings.
     *
     * @return an array of Resource objects, or {@code null} if none
     * @see #getConfigLocations()
     */
    protected Resource[] getConfigResources() {
        return null;
    }
}
