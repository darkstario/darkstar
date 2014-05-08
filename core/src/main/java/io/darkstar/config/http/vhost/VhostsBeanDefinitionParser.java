package io.darkstar.config.http.vhost;

import io.darkstar.config.spring.yaml.AbstractBeanDefinitionParser;
import io.darkstar.config.spring.yaml.BeanDefinitionParser;
import io.darkstar.config.spring.yaml.BeanDefinitionResult;
import io.darkstar.config.spring.yaml.DefaultBeanDefinitionResult;
import io.darkstar.config.spring.yaml.ParserContext;
import io.darkstar.config.yaml.MappingNode;
import io.darkstar.config.yaml.Node;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.util.CollectionUtils;

public class VhostsBeanDefinitionParser extends AbstractBeanDefinitionParser {

    public static final String DEFAULT_VHOST_RESOLVER_BEAN_ID = "darkstarVirtualHostResolver";

    private final BeanDefinitionParser vhostParser =
            new VirtualHostBeanDefinitionParser(DEFAULT_VHOST_RESOLVER_BEAN_ID);

    @Override
    protected BeanDefinitionResult parseInternal(Node node, ParserContext parserContext) {

        MappingNode vhosts = (node instanceof MappingNode) ? (MappingNode)node : null;

        if (vhosts == null || CollectionUtils.isEmpty(vhosts.getValue())) {
            String msg = "'" + node.getName() + "' configuration must specify one or more virtual host entries.";
            parserContext.getReaderContext().error(msg, vhosts);
            return null;
        }

        GenericBeanDefinition def = new GenericBeanDefinition();
        def.setBeanClass(VirtualHostResolverFactoryBean.class);

        //because of the removal, children now contains only connector definitions:
        for (Node childNode : vhosts.getValue().values()) {
            vhostParser.parse(childNode, parserContext);
        }

        return new DefaultBeanDefinitionResult(def, DEFAULT_VHOST_RESOLVER_BEAN_ID);
    }
}
