package io.darkstar.config.http.vhost;

import io.darkstar.http.DefaultVirtualHost;
import io.darkstar.net.Host;
import io.darkstar.net.HostParser;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.ManagedSet;
import org.springframework.beans.factory.yaml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.yaml.BeanDefinitionResult;
import org.springframework.beans.factory.yaml.MappingNode;
import org.springframework.beans.factory.yaml.Node;
import org.springframework.beans.factory.yaml.ParserContext;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class VirtualHostBeanDefinitionParser extends AbstractBeanDefinitionParser {

    private final String vhostStoreBeanId;

    public VirtualHostBeanDefinitionParser(String vhostStoreBeanId) {
        this.vhostStoreBeanId = vhostStoreBeanId;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected BeanDefinitionResult parseInternal(Node node, ParserContext parserContext) {

        MappingNode vhostNode = (node instanceof MappingNode) ? (MappingNode)node : null;

        if (vhostNode == null || CollectionUtils.isEmpty(vhostNode.getValue())) {
            String msg = "'" + node.getName() + "' configuration must specify one or more routes.";
            parserContext.getReaderContext().error(msg, vhostNode);
            return null;
        }

        String vhostName = vhostNode.getName();

        Set<String> vhostNames = StringUtils.commaDelimitedListToSet(vhostName);
        vhostName = vhostNames.iterator().next(); //first is always the primary
        vhostNames.remove(vhostName); //what remains are aliases
        Set<String> aliases = new LinkedHashSet<>(vhostNames);

        Host originHost = null;

        Map<String,Node> values = vhostNode.getValue();

        for(Map.Entry<String,Node> entry : values.entrySet()) {

            String name = entry.getKey();
            Node value = entry.getValue();

            switch(name) {
                case "origin":
                    String originValue = String.valueOf(value.getValue());
                    try {
                        originHost = HostParser.INSTANCE.parse(originValue, 80);
                    } catch (Exception e) {
                        String msg = "Unable to read origin Host value '" + originValue + "'";
                        parserContext.getReaderContext().error(msg, value, e);
                        return null;
                    }

                    break;
                default:
                    parseChild(value, parserContext);
            }
        }

        DefaultVirtualHost vhost = new DefaultVirtualHost(vhostName, aliases, originHost);

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
