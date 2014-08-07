package org.springframework.beans.factory.yaml;

import org.springframework.util.Assert;

/**
 * BeanDefinitionParser for definitions that merely act as a container for other definitions.  This does not
 * create any bean definitions itself - it merely delegates parsing responsibilities to other parsers for each
 * child node encountered.
 */
public class ContainerBeanDefinitionParser extends AbstractBeanDefinitionParser {

    @Override
    protected BeanDefinitionResult parseInternal(Node node, ParserContext parserContext) {

        Assert.isInstanceOf(MappingNode.class, node, "'" + node.getName() +
                "' configuration must be a YAML Mapping node.");

        MappingNode httpNode = (MappingNode) node;

        for (Node childNode : httpNode.getValue().values()) {
            BeanDefinitionParser parser = getBeanDefinitionParser(childNode, parserContext);
            parser.parse(childNode, parserContext);
        }

        return null; //nothing to return - bean definitions will have been created by resolved parsers
    }

    protected BeanDefinitionParser getBeanDefinitionParser(Node node, ParserContext parserContext) {
        BeanDefinitionParserResolver foo = parserContext.getReaderContext().getBeanDefinitionParserResolver();
        return foo.resolveParser(node);
    }
}
