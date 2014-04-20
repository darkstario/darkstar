package io.darkstar.config.spring.yaml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.core.env.Environment;

public class DefaultBeanDefinitionContentReader implements BeanDefinitionContentReader {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private Environment environment;

    private YamlReaderContext readerContext;

    /**
     * {@inheritDoc}
     * <p>Default value is {@code null}; property is required for parsing any YAML element corresponding to a specific
     * {@code profile}.
     *
     * @see #doRegisterBeanDefinitions
     */
    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerBeanDefinitions(Object content, YamlReaderContext readerContext) throws BeanDefinitionStoreException {
        this.readerContext = readerContext;
        logger.debug("Loading bean definitions");
        doRegisterBeanDefinitions(content);
    }

    protected void doRegisterBeanDefinitions(Object content) {
        //To change body of created methods use File | Settings | File Templates.
    }


}
