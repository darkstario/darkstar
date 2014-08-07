package org.springframework.beans.factory.yaml;

public interface BeanDefinitionParserResolver {

    BeanDefinitionParser resolveParser(Node node);
}
