package org.springframework.beans.factory.yaml;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.util.Assert;

public class DefaultBeanDefinitionResult implements BeanDefinitionResult {

    private final String beanName;
    private final String[] beanAliases;
    private final BeanDefinition beanDefinition;

    public DefaultBeanDefinitionResult(BeanDefinition beanDefinition) {
        this(beanDefinition, null, null);
    }

    public DefaultBeanDefinitionResult(BeanDefinition beanDefinition, String beanName) {
        this(beanDefinition, beanName, null);
    }

    public DefaultBeanDefinitionResult(BeanDefinition beanDefinition, String beanName, String[] beanAliases) {
        Assert.notNull(beanDefinition, "BeanDefinition is required.");
        this.beanDefinition = beanDefinition;
        this.beanName = beanName;
        this.beanAliases = beanAliases;
    }

    @Override
    public String getBeanName() {
        return beanName;
    }

    @Override
    public String[] getBeanAliases() {
        return beanAliases;
    }

    @Override
    public BeanDefinition getBeanDefinition() {
        return beanDefinition;
    }

    @Override
    public Object getSource() {
        return getBeanDefinition().getSource();
    }
}
