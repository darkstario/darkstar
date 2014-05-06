package io.darkstar.config.spring.yaml;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.factory.config.BeanDefinition;

public interface BeanDefinitionResult extends BeanMetadataElement {

    String getBeanName();

    String[] getBeanAliases();

    BeanDefinition getBeanDefinition();

}
