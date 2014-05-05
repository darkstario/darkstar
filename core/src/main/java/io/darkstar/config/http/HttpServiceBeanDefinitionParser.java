package io.darkstar.config.http;

import com.stormpath.sdk.lang.Assert;
import io.darkstar.config.spring.yaml.AbstractBeanDefinitionParser;
import io.darkstar.config.spring.yaml.BeanDefinitionParserResolver;
import io.darkstar.config.spring.yaml.ParserContext;
import io.darkstar.config.yaml.MappingNode;
import io.darkstar.config.yaml.Node;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;

import java.util.Map;

public class HttpServiceBeanDefinitionParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Node node, ParserContext parserContext) {

        Assert.isInstanceOf(MappingNode.class, node, "http configuration must be a YAML Mapping node.");

        MappingNode httpNode = (MappingNode)node;

        for(Map.Entry<String,Node> entry : httpNode.getValue().entrySet()) {

            BeanDefinitionParserResolver

        }








        GenericBeanDefinition def = new GenericBeanDefinition();


        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
