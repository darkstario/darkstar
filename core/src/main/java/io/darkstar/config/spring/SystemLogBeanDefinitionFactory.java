package io.darkstar.config.spring;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.FileAppender;
import io.darkstar.config.json.SystemLogConfig;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

public class SystemLogBeanDefinitionFactory implements BeanDefinitionFactory<SystemLogConfig> {

    public Map<String,BeanDefinition> createBeanDefinitions(String beanNamePrefix, SystemLogConfig src) {

        String prefix = beanNamePrefix != null ? beanNamePrefix : "";

        Map<String,BeanDefinition> defs = new LinkedHashMap<>();

        String encoderBeanName = prefix + "SystemLogEncoder";

        defs.put(encoderBeanName,
                BeanDefinitionBuilder.genericBeanDefinition(PatternLayoutEncoder.class)
                        .setInitMethodName("start").setDestroyMethodName("stop")
                        .addPropertyValue("context", LoggerFactory.getILoggerFactory())
                        .addPropertyValue("pattern", src.getFormat()).getBeanDefinition());

        defs.put(prefix + "SystemLogAppender",
                BeanDefinitionBuilder.genericBeanDefinition(FileAppender.class)
                        .setInitMethodName("start").setDestroyMethodName("stop")
                        .addPropertyValue("context", LoggerFactory.getILoggerFactory())
                        .addPropertyValue("file", src.getPath())
                        .addPropertyValue("encoder", new RuntimeBeanReference(encoderBeanName))
                        .getBeanDefinition());

        return defs;
    }
}
