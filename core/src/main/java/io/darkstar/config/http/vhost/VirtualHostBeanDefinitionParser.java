package io.darkstar.config.http.vhost;

import io.darkstar.config.spring.yaml.AbstractBeanDefinitionParser;
import io.darkstar.config.spring.yaml.BeanDefinitionResult;
import io.darkstar.config.spring.yaml.ParserContext;
import io.darkstar.config.yaml.MappingNode;
import io.darkstar.config.yaml.Node;
import io.darkstar.http.DefaultVirtualHost;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.ManagedSet;
import org.springframework.util.CollectionUtils;

public class VirtualHostBeanDefinitionParser extends AbstractBeanDefinitionParser {

    private final String vhostStoreBeanId;

    public VirtualHostBeanDefinitionParser(String vhostStoreBeanId) {
        this.vhostStoreBeanId = vhostStoreBeanId;
    }

    @Override
    protected BeanDefinitionResult parseInternal(Node node, ParserContext parserContext) {

        MappingNode vhostNode = (node instanceof MappingNode) ? (MappingNode)node : null;

        if (vhostNode == null || CollectionUtils.isEmpty(vhostNode.getValue())) {
            String msg = "'" + node.getName() + "' configuration must specify one or more routes.";
            parserContext.getReaderContext().error(msg, vhostNode);
            return null;
        }

        String vhostName = vhostNode.getName();

        DefaultVirtualHost vhost = new DefaultVirtualHost(vhostName, null);

        //todo:
        /*
        Map<String,Node> values = vhostNode.getValue();

        for(Map.Entry<String,Node> entry : values.entrySet()) {

            String name = entry.getKey();
            Node value = entry.getValue();

            switch(name) {

                case "aliases":
                    //todo

                    break;

                //todo: default
            }
        }
        */

        BeanDefinition vhostStoreDef = parserContext.getRegistry().getBeanDefinition(this.vhostStoreBeanId);
        ManagedSet set = (ManagedSet)vhostStoreDef.getPropertyValues().get("virtualHosts");
        if (set == null) {
            set = new ManagedSet<>();
            vhostStoreDef.getPropertyValues().add("virtualHosts", set);
        }

        set.add(vhost);

        return null;
    }
}
