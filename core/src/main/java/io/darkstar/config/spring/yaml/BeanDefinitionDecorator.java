package io.darkstar.config.spring.yaml;

import io.darkstar.config.yaml.Node;
import org.springframework.beans.factory.config.BeanDefinitionHolder;

/**
 * Interface used by the {@link DefaultBeanDefinitionDocumentReader} to handle custom, nested (directly under a {@code
 * &lt;bean&gt;}) tags.
 *
 * <p>Decoration may also occur based on custom attributes applied to the {@code &lt;bean&gt;} tag. Implementations are
 * free to turn the metadata in the custom tag into as many {@link org.springframework.beans.factory.config.BeanDefinition
 * BeanDefinitions} as required and to transform the {@link org.springframework.beans.factory.config.BeanDefinition} of
 * the enclosing {@code &lt;bean&gt;} tag, potentially even returning a completely different {@link
 * org.springframework.beans.factory.config.BeanDefinition} to replace the original.
 *
 * <p>{@link BeanDefinitionDecorator BeanDefinitionDecorators} should be aware that they may be part of a chain. In
 * particular, a {@link BeanDefinitionDecorator} should be aware that a previous {@link BeanDefinitionDecorator} may
 * have replaced the original {@link org.springframework.beans.factory.config.BeanDefinition} with a {@link
 * org.springframework.aop.framework.ProxyFactoryBean} definition allowing for custom {@link
 * org.aopalliance.intercept.MethodInterceptor interceptors} to be added.
 *
 * <p>{@link BeanDefinitionDecorator BeanDefinitionDecorators} that wish to add an interceptor to the enclosing bean
 * should extend {@link org.springframework.aop.config.AbstractInterceptorDrivenBeanDefinitionDecorator} which handles
 * the chaining ensuring that only one proxy is created and that it contains all interceptors from the chain.
 *
 * <p>The parser locates a {@link BeanDefinitionDecorator} from the {@link NodeHandler} for the namespace in which the
 * custom tag resides.
 *
 * @see NodeHandler
 * @see BeanDefinitionParser
 */
public interface BeanDefinitionDecorator {

    /**
     * Parse the specified {@link Node} (either an element or an attribute) and decorate the supplied {@link
     * org.springframework.beans.factory.config.BeanDefinition}, returning the decorated definition.
     *
     * <p>Implementations may choose to return a completely new definition, which will replace the original definition
     * in the resulting {@link org.springframework.beans.factory.BeanFactory}.
     *
     * <p>The supplied {@link ParserContext} can be used to register any additional beans needed to support the main
     * definition.
     *
     * @param node the node being processed
     * @param definition the existing bean definition
     * @param parserContext the parser context
     */
    BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definition, ParserContext parserContext);
}
