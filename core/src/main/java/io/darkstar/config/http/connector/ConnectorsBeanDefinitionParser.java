package io.darkstar.config.http.connector;

import io.darkstar.config.spring.yaml.AbstractBeanDefinitionParser;
import io.darkstar.config.spring.yaml.BeanDefinitionParser;
import io.darkstar.config.spring.yaml.BeanDefinitionResult;
import io.darkstar.config.spring.yaml.DefaultBeanDefinitionResult;
import io.darkstar.config.spring.yaml.ParserContext;
import io.darkstar.config.yaml.MappingNode;
import io.darkstar.config.yaml.Node;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.util.CollectionUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class ConnectorsBeanDefinitionParser extends AbstractBeanDefinitionParser {

    public static final String DEFAULT_WORKER_GROUP_BEAN_ID = "darkstarNettyWorkerGroup";

    private static final String WORKERS_NODE = "workers";

    private final BeanDefinitionParser connectorParser =
            new ConnectorBeanDefinitionParser(DEFAULT_WORKER_GROUP_BEAN_ID);

    @Override
    protected BeanDefinitionResult parseInternal(Node node, ParserContext parserContext) {

        MappingNode connectors = (node instanceof MappingNode) ? (MappingNode)node : null;

        if (connectors == null || CollectionUtils.isEmpty(connectors.getValue())) {
            String msg = "'" + node.getName() + "' configuration must specify one or more connector entries.";
            parserContext.getReaderContext().error(msg, connectors);
            return null;
        }

        //create a copy so we don't alter the source map:
        Map<String,Node> children = new LinkedHashMap<>(connectors.getValue());

        GenericBeanDefinition def = new GenericBeanDefinition();
        def.setBeanClass(NioEventLoopGroupFactoryBean.class);

        //now remove the workers node if there is one:
        Node workersNode = children.remove(WORKERS_NODE);
        if (workersNode != null) {
            String value = String.valueOf(workersNode.getValue());
            def.getPropertyValues().add("numThreads", value);
        }

        //because of the removal, children now contains only connector definitions:
        for (Node childNode : children.values()) {
            connectorParser.parse(childNode, parserContext);
        }

        return new DefaultBeanDefinitionResult(def, DEFAULT_WORKER_GROUP_BEAN_ID);
    }

}
