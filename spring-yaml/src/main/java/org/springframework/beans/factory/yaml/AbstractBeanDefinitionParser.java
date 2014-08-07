package org.springframework.beans.factory.yaml;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.util.StringUtils;

/**
 * Abstract {@link BeanDefinitionParser} implementation providing a number of convenience methods and a
 * {@link AbstractBeanDefinitionParser#parseInternal template method} that subclasses must override to provide the
 * actual parsing logic.
 *
 * <p>Use this {@link BeanDefinitionParser} implementation when you want to parse some arbitrarily complex YAML into
 * one or more {@link org.springframework.beans.factory.config.BeanDefinition BeanDefinitions}. If you just want to
 * parse some YAML into a single {@code BeanDefinition}, you may wish to consider the simpler convenience extensions
 * of this class, namely {@link AbstractSingleBeanDefinitionParser}.
 *
 * @since 0.1
 */
public abstract class AbstractBeanDefinitionParser implements BeanDefinitionParser {

    @Override
    public final BeanDefinition parse(Node node, ParserContext parserContext) {

        BeanDefinitionResult result = parseInternal(node, parserContext);

        if (result != null && !parserContext.isNested()) {
            try {

                String id = result.getBeanName();

                if (!StringUtils.hasText(id)) {
                    id = resolveId(node, (AbstractBeanDefinition)result.getBeanDefinition(), parserContext);
                    if (!StringUtils.hasText(id)) {
                        parserContext.getReaderContext().error(
                                "Name is required for config node '" + node + "' when used as a top-level node", node);
                    }
                }

                //TODO: support auto-naming aliases?
                /*
                String[] aliases = new String[0];
                String name = node.getName();
                if (StringUtils.hasLength(name)) {
                    aliases = StringUtils.trimArrayElements(StringUtils.commaDelimitedListToStringArray(name));
                }
                */
                BeanDefinitionHolder holder = new BeanDefinitionHolder(result.getBeanDefinition(), id, result.getBeanAliases());
                registerBeanDefinition(holder, parserContext.getRegistry());
                if (shouldFireEvents()) {
                    BeanComponentDefinition componentDefinition = new BeanComponentDefinition(holder);
                    postProcessComponentDefinition(componentDefinition);
                    parserContext.registerComponent(componentDefinition);
                }
            } catch (BeanDefinitionStoreException ex) {
                parserContext.getReaderContext().error(ex.getMessage(), node);
                return null;
            }
        }

        if (result != null) {
            return result.getBeanDefinition();
        }

        return null;
    }

    /**
     * Resolve the ID for the supplied {@link BeanDefinition}.
     * <p>When using {@link #shouldGenerateId generation}, a name is generated automatically.
     * Otherwise, the ID is extracted from the "id" attribute, potentially with a
     * {@link #shouldGenerateIdAsFallback() fallback} to a generated id.
     *
     * @param node       the node that the bean definition has been built from
     * @param definition    the bean definition to be registered
     * @param parserContext the object encapsulating the current state of the parsing process;
     *                      provides access to a {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}
     * @return the resolved id
     * @throws BeanDefinitionStoreException if no unique name could be generated
     *                                      for the given bean definition
     */
    protected String resolveId(Node node, AbstractBeanDefinition definition, ParserContext parserContext)
            throws BeanDefinitionStoreException {

        if (shouldGenerateId()) {
            return parserContext.getReaderContext().generateBeanName(definition);
        } else {
            String id = node.getName();
            if (!StringUtils.hasText(id) && shouldGenerateIdAsFallback()) {
                id = parserContext.getReaderContext().generateBeanName(definition);
            }
            return id;
        }
    }

    /**
     * Register the supplied {@link BeanDefinitionHolder bean} with the supplied
     * {@link org.springframework.beans.factory.support.BeanDefinitionRegistry registry}.
     * <p>Subclasses can override this method to control whether or not the supplied
     * {@link BeanDefinitionHolder bean} is actually even registered, or to
     * register even more beans.
     * <p>The default implementation registers the supplied {@link BeanDefinitionHolder bean}
     * with the supplied {@link org.springframework.beans.factory.support.BeanDefinitionRegistry registry} only if the {@code isNested}
     * parameter is {@code false}, because one typically does not want inner beans
     * to be registered as top level beans.
     *
     * @param definition the bean definition to be registered
     * @param registry   the registry that the bean is to be registered with
     * @see org.springframework.beans.factory.support.BeanDefinitionReaderUtils#registerBeanDefinition(BeanDefinitionHolder, org.springframework.beans.factory.support.BeanDefinitionRegistry)
     */
    protected void registerBeanDefinition(BeanDefinitionHolder definition, BeanDefinitionRegistry registry) {
        BeanDefinitionReaderUtils.registerBeanDefinition(definition, registry);
    }

    protected void registerBeanDefinition(BeanDefinitionRegistry registry,
                                          BeanDefinition definition,
                                          String beanName, String... aliases) {
        registerBeanDefinition(new BeanDefinitionHolder(definition, beanName, aliases), registry);
    }

    protected void parseChild(Node childNode, ParserContext parserContext) {
        BeanDefinitionParser parser = parserContext.getReaderContext()
                .getBeanDefinitionParserResolver().resolveParser(childNode);
        parser.parse(childNode, parserContext);
    }

    /**
     * Central template method to actually parse the supplied {@link Node}
     * into one or more {@link BeanDefinition BeanDefinitions}.
     *
     * @param node       the node that is to be parsed into one or more {@link BeanDefinition BeanDefinitions}
     * @param parserContext the object encapsulating the current state of the parsing process;
     *                      provides access to a {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}
     * @return the primary {@link BeanDefinition} resulting from the parsing of the supplied {@link Node}
     * @see #parse(Node, ParserContext)
     * @see #postProcessComponentDefinition(org.springframework.beans.factory.parsing.BeanComponentDefinition)
     */
    protected abstract BeanDefinitionResult parseInternal(Node node, ParserContext parserContext);

    /**
     * Should an ID be generated instead of read from the passed in {@link Node}?
     * <p>Disabled by default; subclasses can override this to enable ID generation.
     * Note that this flag is about <i>always</i> generating an ID; the parser
     * won't even check for an "id" attribute in this case.
     *
     * @return whether the parser should always generate an id
     */
    protected boolean shouldGenerateId() {
        return false;
    }

    /**
     * Should an ID be generated instead if the passed in {@link Node} does not
     * specify an "id" attribute explicitly?
     * <p>Disabled by default; subclasses can override this to enable ID generation
     * as fallback: The parser will first check for an "id" attribute in this case,
     * only falling back to a generated ID if no value was specified.
     *
     * @return whether the parser should generate an id if no id was specified
     */
    protected boolean shouldGenerateIdAsFallback() {
        return false;
    }

    /**
     * Controls whether this parser is supposed to fire a
     * {@link org.springframework.beans.factory.parsing.BeanComponentDefinition}
     * event after parsing the bean definition.
     * <p>This implementation returns {@code true} by default; that is,
     * an event will be fired when a bean definition has been completely parsed.
     * Override this to return {@code false} in order to suppress the event.
     *
     * @return {@code true} in order to fire a component registration event
     *         after parsing the bean definition; {@code false} to suppress the event
     * @see #postProcessComponentDefinition
     * @see org.springframework.beans.factory.parsing.ReaderContext#fireComponentRegistered
     */
    protected boolean shouldFireEvents() {
        return true;
    }

    /**
     * Hook method called after the primary parsing of a
     * {@link BeanComponentDefinition} but before the
     * {@link BeanComponentDefinition} has been registered with a
     * {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}.
     * <p>Derived classes can override this method to supply any custom logic that
     * is to be executed after all the parsing is finished.
     * <p>The default implementation is a no-op.
     *
     * @param componentDefinition the {@link BeanComponentDefinition} that is to be processed
     */
    protected void postProcessComponentDefinition(BeanComponentDefinition componentDefinition) {
    }
}
