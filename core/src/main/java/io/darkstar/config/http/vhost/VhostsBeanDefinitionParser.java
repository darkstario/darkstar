package io.darkstar.config.http.vhost;

import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.yaml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.yaml.BeanDefinitionParser;
import org.springframework.beans.factory.yaml.BeanDefinitionResult;
import org.springframework.beans.factory.yaml.DefaultBeanDefinitionResult;
import org.springframework.beans.factory.yaml.MappingNode;
import org.springframework.beans.factory.yaml.Node;
import org.springframework.beans.factory.yaml.ParserContext;
import org.springframework.util.CollectionUtils;

public class VhostsBeanDefinitionParser extends AbstractBeanDefinitionParser {

    public static final String DEFAULT_VHOST_RESOLVER_BEAN_ID = "darkstarVirtualHostResolver";

    public static final String DEFAULT_VHOST_STORE_BEAN_ID = "darkstarVirtualHostStore";

    private final BeanDefinitionParser vhostParser = new VirtualHostBeanDefinitionParser(DEFAULT_VHOST_STORE_BEAN_ID);

    @Override
    protected BeanDefinitionResult parseInternal(Node node, ParserContext parserContext) {

        MappingNode vhosts = (node instanceof MappingNode) ? (MappingNode) node : null;

        if (vhosts == null || CollectionUtils.isEmpty(vhosts.getValue())) {
            String msg = "'" + node.getName() + "' configuration must specify one or more virtual host entries.";
            parserContext.getReaderContext().error(msg, vhosts);
            return null;
        }

        GenericBeanDefinition vhostStoreDef = new GenericBeanDefinition();
        vhostStoreDef.setBeanClass(VirtualHostStoreFactoryBean.class);
        registerBeanDefinition(parserContext.getRegistry(), vhostStoreDef, DEFAULT_VHOST_STORE_BEAN_ID);

        GenericBeanDefinition def = new GenericBeanDefinition();
        def.setBeanClass(VirtualHostResolverFactoryBean.class);
        def.getPropertyValues().add("virtualHostStore", new RuntimeBeanReference(DEFAULT_VHOST_STORE_BEAN_ID));

        //because of the removal, children now contains only connector definitions:
        for (Node childNode : vhosts.getValue().values()) {
            vhostParser.parse(childNode, parserContext);
        }

        return new DefaultBeanDefinitionResult(def, DEFAULT_VHOST_RESOLVER_BEAN_ID);
    }
}
