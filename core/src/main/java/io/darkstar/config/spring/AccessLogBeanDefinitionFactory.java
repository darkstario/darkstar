package io.darkstar.config.spring;

import ch.qos.logback.access.PatternLayoutEncoder;
import ch.qos.logback.core.FileAppender;
import io.darkstar.config.json.LogConfig;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

public class AccessLogBeanDefinitionFactory implements BeanDefinitionFactory<LogConfig> {

    @Override
    public Map<String, BeanDefinition> createBeanDefinitions(String beanNamePrefix, LogConfig src) {
        String prefix = beanNamePrefix != null ? beanNamePrefix : "";

        Map<String,BeanDefinition> defs = new LinkedHashMap<>();

        String encoderBeanName = prefix + "AccessLogEncoder";

        defs.put(encoderBeanName,
                BeanDefinitionBuilder.genericBeanDefinition(PatternLayoutEncoder.class)
                        .setInitMethodName("start").setDestroyMethodName("stop")
                        .addPropertyValue("context", LoggerFactory.getILoggerFactory())
                        .addPropertyValue("pattern", src.getFormat()).getBeanDefinition());

        defs.put(prefix + "AccessLogAppender",
                BeanDefinitionBuilder.genericBeanDefinition(FileAppender.class)
                        .setInitMethodName("start").setDestroyMethodName("stop")
                        .addPropertyValue("context", LoggerFactory.getILoggerFactory())
                        .addPropertyValue("file", src.getPath())
                        .addPropertyValue("encoder", new RuntimeBeanReference(encoderBeanName))
                        .getBeanDefinition());

        return defs;
    }
}
