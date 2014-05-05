package io.darkstar.config.spring.yaml;

import io.darkstar.config.yaml.CollectionNode;
import io.darkstar.config.yaml.Node;
import io.darkstar.config.yaml.ScalarNode;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * Support class for implementing custom {@link NodeHandler NodeHandlers}.  Parsing and decorating of individual
 * {@link Node Nodes} is done via {@link BeanDefinitionParser} and {@link BeanDefinitionDecorator} strategy interfaces,
 * respectively.
 * <p>
 * Provides the {@link #registerBeanDefinitionParser} and {@link #registerBeanDefinitionDecorator} methods for
 * registering a {@link BeanDefinitionParser} or {@link BeanDefinitionDecorator} to handle a specific element.
 * </p>
 *
 * @see #registerBeanDefinitionParser(String, BeanDefinitionParser)
 * @see #registerBeanDefinitionDecorator(String, BeanDefinitionDecorator)
 */
public abstract class NodeHandlerSupport implements NodeHandler {

    /**
     * Stores the {@link BeanDefinitionParser} implementations keyed by the local name of the {@link Node Nodes}
     * they handle.
     */
    private final Map<String, BeanDefinitionParser> parsers = new HashMap<>();

    /**
     * Stores the {@link BeanDefinitionDecorator} implementations keyed by the local name of the {@link Node Nodes}
     * they handle.
     */
    private final Map<String, BeanDefinitionDecorator> decorators = new HashMap<>();

    /**
     * Stores the {@link BeanDefinitionDecorator} implementations keyed by the local name of the {@link Node Nodes}
     * they handle.
     */
    private final Map<String, BeanDefinitionDecorator> attributeDecorators = new HashMap<>();


    /**
     * Parses the supplied {@link Node} by delegating to the {@link BeanDefinitionParser} that is registered for that
     * {@link Node}.
     */
    @Override
    public BeanDefinition parse(Node node, ParserContext parserContext) {
        return findParserForElement(node, parserContext).parse(node, parserContext);
    }

    /**
     * Locates the {@link BeanDefinitionParser} from the registered implementations using the {@link Node} name.
     */
    private BeanDefinitionParser findParserForElement(Node node, ParserContext parserContext) {
        String name = node.getName();
        BeanDefinitionParser parser = this.parsers.get(name);
        if (parser == null) {
            String msg = "Cannot locate BeanDefinitionParser for node [" + name + "]";
            parserContext.getReaderContext().fatal(msg, node);
        }
        return parser;
    }

    /**
     * Decorates the supplied {@link Node} by delegating to the {@link BeanDefinitionDecorator} that is registered to
     * handle that {@link Node}.
     */
    @Override
    public BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definition, ParserContext parserContext) {
        return findDecoratorForNode(node, parserContext).decorate(node, definition, parserContext);
    }

    /**
     * Locates the {@link BeanDefinitionParser} from the register implementations using the {@link Node} name.
     */
    private BeanDefinitionDecorator findDecoratorForNode(Node node, ParserContext parserContext) {
        BeanDefinitionDecorator decorator = null;
        String name = node.getName();
        if (node instanceof CollectionNode) {
            decorator = this.decorators.get(name);
        } else if (node instanceof ScalarNode) {
            decorator = this.attributeDecorators.get(name);
        } else {
            String msg = "Cannot decorate based on Nodes of type [" + node.getClass().getName() + "]";
            parserContext.getReaderContext().fatal(msg, node);
        }
        if (decorator == null) {
            String msg = "Cannot locate BeanDefinitionDecorator for " +
                    (node instanceof CollectionNode ? "node" : "attribute") + " [" + name + "]";
            parserContext.getReaderContext().fatal(msg, node);
        }
        return decorator;
    }


    /**
     * Subclasses can call this to register the supplied {@link BeanDefinitionParser} to handle nodes with the
     * specified name.
     *
     * @param nodeName the name of the node to handle
     * @param parser   the bean definition parser
     */
    protected final void registerBeanDefinitionParser(String nodeName, BeanDefinitionParser parser) {
        this.parsers.put(nodeName, parser);
    }

    /**
     * Subclasses can call this to register the supplied {@link BeanDefinitionDecorator} to handle nodes with the
     * specified name.
     *
     * @param nodeName the name of the node to handle
     * @param dec      bean definition decorator
     */
    @SuppressWarnings("UnusedDeclaration")
    protected final void registerBeanDefinitionDecorator(String nodeName, BeanDefinitionDecorator dec) {
        this.decorators.put(nodeName, dec);
    }

    /**
     * Subclasses can call this to register the supplied {@link BeanDefinitionDecorator} to handle attribute nodes with
     * the specified name.
     *
     * @param nodeName the name of the attribute node
     * @param dec      bean definition decorator
     */
    @SuppressWarnings("UnusedDeclaration")
    protected final void registerBeanDefinitionDecoratorForAttribute(String nodeName, BeanDefinitionDecorator dec) {
        this.attributeDecorators.put(nodeName, dec);
    }
}
