package io.darkstar.config.spring.yaml;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.core.env.Environment;

/**
 * SPI for parsing YAML document that contains Spring bean definitions.  Used by YamlBeanDefinitionReader for actually
 * parsing a YAML document.
 * <p>
 * Instantiated per document to parse: Implementations can hold state in instance variables during the execution of
 * the {@link #registerBeanDefinitions} method, for example global settings that are defined for all bean definitions
 * in the document.
 * </p>
 *
 * @see YamlBeanDefinitionReader#setContentReaderClass
 */
public interface BeanDefinitionContentReader {

    /**
     * Set the Environment to use when reading bean definitions.
     * <p>Used for evaluating profile information to determine whether a document/element should be included or
     * ignored.</p>
     */
    void setEnvironment(Environment environment);

    /**
     * Read bean definitions from the given YAML document and register them with the registry in the given reader
     * context.
     *
     * @param content       the YAML document
     * @param readerContext the current context of the reader
     *                      (includes the target registry and the resource being parsed)
     * @throws BeanDefinitionStoreException in case of parsing errors
     */
    void registerBeanDefinitions(Object content, YamlReaderContext readerContext)
            throws BeanDefinitionStoreException;

}

