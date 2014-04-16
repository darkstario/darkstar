package io.darkstar.config.spring;

import org.springframework.beans.factory.config.BeanDefinition;

import java.util.Map;

public interface BeanDefinitionFactory<T> {

    public Map<String,BeanDefinition> createBeanDefinitions(String beanNamePrefix, T o);
}
