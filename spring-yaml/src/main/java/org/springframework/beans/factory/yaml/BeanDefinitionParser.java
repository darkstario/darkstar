package org.springframework.beans.factory.yaml;

import org.springframework.beans.factory.config.BeanDefinition;

/**
 * Interface used by the {@link DefaultBeanDefinitionDocumentReader} to handle custom (plugin-specific) YAML
 * configuration nodes.
 *
 * <p>Implementations are free to turn the metadata in the custom tag into as many {@link
 * org.springframework.beans.factory.config.BeanDefinition BeanDefinitions} as required.
 *
 * @see AbstractBeanDefinitionParser
 */
public interface BeanDefinitionParser {

    /**
     * Parse the specified {@link Node Node} and register the resulting {@link BeanDefinition BeanDefinition(s)} with
     * the {@link ParserContext#getRegistry() BeanDefinitionRegistry} embedded in the supplied {@link ParserContext}.
     *
     * <p>Implementations must return the primary {@link BeanDefinition} that results from the parse if they will ever
     * be used in a nested fashion (for example as an inner tag in a {@code <property/>} tag). Implementations may
     * return {@code null} if they will <strong>not</strong> be used in a nested fashion.</p>
     *
     * @param node          the node that is to be parsed into one or more {@link BeanDefinition BeanDefinitions}
     * @param parserContext the object encapsulating the current state of the parsing process; provides access to a
     *                      {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}
     * @return the primary {@link BeanDefinition}
     */
    BeanDefinition parse(Node node, ParserContext parserContext);
}
