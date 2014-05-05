package io.darkstar.config.spring.yaml;

import io.darkstar.config.yaml.Node;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;

/**
 * Base interface used by the {@link DefaultBeanDefinitionDocumentReader}
 * for handling custom namespaces in a Spring XML configuration file.
 *
 * <p>Implementations are expected to return implementations of the
 * {@link BeanDefinitionParser} interface for custom top-level tags and
 * implementations of the {@link BeanDefinitionDecorator} interface for
 * custom nested tags.
 *
 * <p>The parser will call {@link #parse} when it encounters a custom tag
 * directly under the {@code &lt;beans&gt;} tags and {@link #decorate} when
 * it encounters a custom tag directly under a {@code &lt;bean&gt;} tag.
 *
 * <p>Developers writing their own custom element extensions typically will
 * not implement this interface directly, but rather make use of the provided
 * {@link NodeHandlerSupport} class.
 *
 * @see DefaultBeanDefinitionDocumentReader
 * @see NodeHandlerResolver
 */
public interface NodeHandler {

    /**
     * Invoked by the {@link org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader} after
     * construction but before any custom elements are parsed.
     * @see NodeHandlerSupport#registerBeanDefinitionParser(String, BeanDefinitionParser)
     */
    void init();

    /**
     * Parse the specified {@link org.w3c.dom.Element} and register any resulting
     * {@link org.springframework.beans.factory.config.BeanDefinition BeanDefinitions} with the
     * {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}
     * that is embedded in the supplied {@link org.springframework.beans.factory.xml.ParserContext}.
     * <p>Implementations should return the primary {@code BeanDefinition}
     * that results from the parse phase if they wish to be used nested
     * inside (for example) a {@code &lt;property&gt;} tag.
     * <p>Implementations may return {@code null} if they will
     * <strong>not</strong> be used in a nested scenario.
     * @param node the element that is to be parsed into one or more {@code BeanDefinitions}
     * @param parserContext the object encapsulating the current state of the parsing process
     * @return the primary {@code BeanDefinition} (can be {@code null} as explained above)
     */
    BeanDefinition parse(Node node, ParserContext parserContext);

    /**
     * Parse the specified {@link org.w3c.dom.Node} and decorate the supplied
     * {@link org.springframework.beans.factory.config.BeanDefinitionHolder}, returning the decorated definition.
     * <p>The {@link org.w3c.dom.Node} may be either an {@link org.w3c.dom.Attr} or an
     * {@link Node}, depending on whether a custom attribute or element
     * is being parsed.
     * <p>Implementations may choose to return a completely new definition,
     * which will replace the original definition in the resulting
     * {@link org.springframework.beans.factory.BeanFactory}.
     * <p>The supplied {@link org.springframework.beans.factory.xml.ParserContext} can be used to register any
     * additional beans needed to support the main definition.
     * @param source the source element or attribute that is to be parsed
     * @param definition the current bean definition
     * @param parserContext the object encapsulating the current state of the parsing process
     * @return the decorated definition (to be registered in the BeanFactory),
     * or simply the original bean definition if no decoration is required.
     * A {@code null} value is strictly speaking invalid, but will be leniently
     * treated like the case where the original bean definition gets returned.
     */
    BeanDefinitionHolder decorate(Node source, BeanDefinitionHolder definition, ParserContext parserContext);
}
