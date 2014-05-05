package io.darkstar.config.spring.yaml;

import io.darkstar.config.yaml.Node;

public interface BeanDefinitionParserResolver {

    BeanDefinitionParser resolveParser(Node node);
}
