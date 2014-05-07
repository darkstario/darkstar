package io.darkstar.config.spring.yaml;

import io.darkstar.config.yaml.Node;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

public class AbstractSingleBeanDefinitionParser extends AbstractBeanDefinitionParser {

    /**
     * Creates a {@link org.springframework.beans.factory.support.BeanDefinitionBuilder} instance for the {@link
     * #getBeanClass bean Class} and passes it to the {@link #doParse} strategy method.
     *
     * @param node          the node that is to be parsed into a single BeanDefinition
     * @param parserContext the object encapsulating the current state of the parsing process
     * @return the BeanDefinition resulting from the parsing of the supplied {@link Node}
     * @throws IllegalStateException if the bean {@link Class} returned from {@link #getBeanClass(Node)} is {@code
     *                               null}
     * @see #doParse
     */
    @Override
    protected final BeanDefinitionResult parseInternal(Node node, ParserContext parserContext) {

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();

        String parentName = getParentName(node);
        if (parentName != null) {
            builder.getRawBeanDefinition().setParentName(parentName);
        }
        Class<?> beanClass = getBeanClass(node);
        if (beanClass != null) {
            builder.getRawBeanDefinition().setBeanClass(beanClass);
        } else {
            String beanClassName = getBeanClassName(node);
            if (beanClassName != null) {
                builder.getRawBeanDefinition().setBeanClassName(beanClassName);
            }
        }
        builder.getRawBeanDefinition().setSource(parserContext.extractSource(node));
        if (parserContext.isNested()) {
            // Inner bean definition must receive same scope as containing bean.
            builder.setScope(parserContext.getContainingBeanDefinition().getScope());
        }
        if (parserContext.isDefaultLazyInit()) {
            // Default-lazy-init applies to custom bean definitions as well.
            builder.setLazyInit(true);
        }
        doParse(node, parserContext, builder);

        return new DefaultBeanDefinitionResult(builder.getBeanDefinition());
    }

    /**
     * Determine the name for the parent of the currently parsed bean, in case of the current bean being defined as a
     * child bean. <p> The default implementation returns {@code null}, indicating a root bean definition. </p>
     *
     * @param node the {@code Node} that is being parsed
     * @return the name of the parent bean for the currently parsed bean, or {@code null} if none
     */
    protected String getParentName(Node node) {
        return null;
    }

    /**
     * Determine the bean class corresponding to the supplied {@link Node}.
     *
     * <p>Note that, for application classes, it is generally preferable to override {@link #getBeanClassName} instead,
     * in order to avoid a direct dependence on the bean implementation class. The BeanDefinitionParsers can be used
     * within an IDE plugin then, even if the application classes are not available on the plugin's classpath.
     *
     * @param node the {@code Node} that is being parsed
     * @return the {@link Class} of the bean that is being defined via parsing the supplied {@code Node}, or {@code
     *         null} if none
     * @see #getBeanClassName
     */
    protected Class<?> getBeanClass(Node node) {
        return null;
    }

    /**
     * Determine the bean class name corresponding to the supplied {@link Node}.
     *
     * @param node the {@code Node} that is being parsed
     * @return the class name of the bean that is being defined via parsing the supplied {@code Node}, or {@code null}
     *         if none
     * @see #getBeanClass
     */
    protected String getBeanClassName(Node node) {
        return null;
    }

    /**
     * Parse the supplied {@link Node} and populate the supplied {@link BeanDefinitionBuilder} as required. <p>The
     * default implementation delegates to the {@code doParse} version without ParserContext argument.
     *
     * @param node          the YAML node being parsed
     * @param parserContext the object encapsulating the current state of the parsing process
     * @param builder       used to define the {@code BeanDefinition}
     * @see #doParse(Node, BeanDefinitionBuilder)
     */
    protected void doParse(Node node, ParserContext parserContext, BeanDefinitionBuilder builder) {
        doParse(node, builder);
    }

    /**
     * Parse the supplied {@link Node} and populate the supplied {@link BeanDefinitionBuilder} as required. <p>The
     * default implementation does nothing.
     *
     * @param node    the YAML node being parsed
     * @param builder used to define the {@code BeanDefinition}
     */
    protected void doParse(Node node, BeanDefinitionBuilder builder) {
    }

}
