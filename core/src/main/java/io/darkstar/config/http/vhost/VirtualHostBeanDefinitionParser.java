package io.darkstar.config.http.vhost;

import io.darkstar.config.spring.yaml.AbstractBeanDefinitionParser;
import io.darkstar.config.spring.yaml.BeanDefinitionResult;
import io.darkstar.config.spring.yaml.ParserContext;
import io.darkstar.config.yaml.Node;

public class VirtualHostBeanDefinitionParser extends AbstractBeanDefinitionParser {

    private final String vhostResolverBeanId;

    public VirtualHostBeanDefinitionParser(String vhostResolverBeanId) {
        this.vhostResolverBeanId = vhostResolverBeanId;
    }

    @Override
    protected BeanDefinitionResult parseInternal(Node node, ParserContext parserContext) {
        //TODO: IMPLEMENT ME!
        return null;
    }
}
