package io.darkstar.config.spring;

public class SystemLogBeanDefinitionFactory {

    /*

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

    */
}
