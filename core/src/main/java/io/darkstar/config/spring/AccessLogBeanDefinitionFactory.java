package io.darkstar.config.spring;

public class AccessLogBeanDefinitionFactory {

    /*

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

    */
}
