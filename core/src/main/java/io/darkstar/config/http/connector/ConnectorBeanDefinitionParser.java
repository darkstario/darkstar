package io.darkstar.config.http.connector;

import io.darkstar.config.spring.yaml.AbstractBeanDefinitionParser;
import io.darkstar.config.spring.yaml.BeanDefinitionResult;
import io.darkstar.config.spring.yaml.DefaultBeanDefinitionResult;
import io.darkstar.config.spring.yaml.ParserContext;
import io.darkstar.config.yaml.MappingNode;
import io.darkstar.config.yaml.Node;
import io.darkstar.config.yaml.ScalarNode;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class ConnectorBeanDefinitionParser extends AbstractBeanDefinitionParser {

    public final String DEFAULT_CHANNEL_INITIALIZER_BEAN_NAME = "frontendInitializer";
    public final String DEFAULT_TLS_CHANNEL_INITIALIZER_BEAN_NAME = "tlsFrontendInitializer";

    private final String workerGroupBeanId;

    public ConnectorBeanDefinitionParser(String workerGroupBeanId) {
        this.workerGroupBeanId = workerGroupBeanId;
    }

    @Override
    protected BeanDefinitionResult parseInternal(Node node, ParserContext parserContext) {

        BeanDefinitionBuilder def = BeanDefinitionBuilder.genericBeanDefinition(ChannelFactoryBean.class);
        if (workerGroupBeanId != null) {
            def.addPropertyReference("workerGroup", workerGroupBeanId);
        }

        boolean tls;

        //check short form:
        if (node instanceof ScalarNode) {
            tls = parseScalar((ScalarNode)node, def, parserContext);
        } else {
            Assert.isInstanceOf(MappingNode.class, node,
                    "Connector complex definitions must be represented as a Mapping node.");
            tls = parseMapping((MappingNode) node, def, parserContext);
        }

        String channelInitializerBeanName = tls ?
                DEFAULT_TLS_CHANNEL_INITIALIZER_BEAN_NAME : DEFAULT_CHANNEL_INITIALIZER_BEAN_NAME;
        def.addPropertyReference("channelInitializer", channelInitializerBeanName);

        String name = "darkstar" + StringUtils.capitalize(node.getName()) + "Channel";
        return new DefaultBeanDefinitionResult(def.getBeanDefinition(), name);
    }

    protected boolean parseScalar(ScalarNode node, BeanDefinitionBuilder def, ParserContext parserContext) {
        String address = String.valueOf(node.getValue());
        def.addPropertyValue("address", address);

        return "https".equals(node.getName());
    }

    protected boolean parseMapping(MappingNode node, BeanDefinitionBuilder def, ParserContext parserContext) {

        boolean tls = "https".equals(node.getName());

        for(Node child : node.getValue().values()) {

            switch(child.getName()) {
                case "address":
                    def.addPropertyValue("address", String.valueOf(child.getValue())); break;
                case "port":
                    def.addPropertyValue("port", String.valueOf(child.getValue())); break;
                /*case "tls":
                    Assert.isInstanceOf(MappingNode.class, child, "Connector 'tls' definition must be a mapping node.");
                    parseTls((MappingNode) child, parserContext); */
                default:
                    parseChild(child, parserContext);
            }
        }

        return tls;
    }

    /*
    protected BeanDefinition parseTls(MappingNode tlsNode, ParserContext parserContext) {

    }
    */

}
