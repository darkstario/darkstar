package io.darkstar.config.spring.yaml;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.beans.factory.parsing.ReaderContext;
import org.springframework.beans.factory.parsing.ReaderEventListener;
import org.springframework.beans.factory.parsing.SourceExtractor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class YamlReaderContext extends ReaderContext {

    private final YamlBeanDefinitionReader reader;

    private final BeanDefinitionParserResolver beanDefinitionParserResolver;

    private final Environment environment;

    public YamlReaderContext(Resource resource, ProblemReporter problemReporter,
                             ReaderEventListener eventListener, SourceExtractor sourceExtractor,
                             YamlBeanDefinitionReader reader, Environment environment,
                             BeanDefinitionParserResolver beanDefinitionParserResolver) {
        super(resource, problemReporter, eventListener, sourceExtractor);
        this.reader = reader;
        this.environment = environment;
        this.beanDefinitionParserResolver = beanDefinitionParserResolver;
    }

    public final YamlBeanDefinitionReader getReader() {
        return this.reader;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public final BeanDefinitionRegistry getRegistry() {
        return this.reader.getRegistry();
    }

    public final ResourceLoader getResourceLoader() {
        return this.reader.getResourceLoader();
    }

    public final ClassLoader getBeanClassLoader() {
        return this.reader.getBeanClassLoader();
    }

    public final BeanDefinitionParserResolver getBeanDefinitionParserResolver() {
        return this.beanDefinitionParserResolver;
    }

    public String generateBeanName(BeanDefinition beanDefinition) {
        return this.reader.getBeanNameGenerator().generateBeanName(beanDefinition, getRegistry());
    }

    public String registerWithGeneratedName(BeanDefinition beanDefinition) {
        String generatedName = generateBeanName(beanDefinition);
        getRegistry().registerBeanDefinition(generatedName, beanDefinition);
        return generatedName;
    }

    /*
    public Document readDocumentFromString(String documentContent) {
        InputSource is = new InputSource(new StringReader(documentContent));
        try {
            return this.reader.doLoadDocument(is, getResource());
        }
        catch (Exception ex) {
            throw new BeanDefinitionStoreException("Failed to read YAML document", ex);
        }
    }
    */

}
