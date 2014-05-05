package io.darkstar.config.spring.yaml;

import com.stormpath.sdk.lang.Assert;
import io.darkstar.config.yaml.MappingNode;
import io.darkstar.config.yaml.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.ParseState;
import org.springframework.core.env.Environment;

import java.util.Map;

public class DefaultBeanDefinitionDocumentReader implements BeanDefinitionDocumentReader {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private Environment environment;

    private YamlReaderContext readerContext;

    private ParseState parseState = new ParseState();

    /**
     * {@inheritDoc}
     * <p>Default value is {@code null}; property is required for parsing any YAML element corresponding to a specific
     * {@code profile}.
     *
     * @see #doRegisterBeanDefinitions
     */
    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerBeanDefinitions(Node node, YamlReaderContext readerContext) throws BeanDefinitionStoreException {
        this.readerContext = readerContext;
        logger.debug("Loading bean definitions");
        doRegisterBeanDefinitions(node);
    }

    protected void doRegisterBeanDefinitions(Node document) {
        Assert.isInstanceOf(MappingNode.class, document, "Root level argument must be a MappingNode");
        assert document instanceof MappingNode;

        MappingNode rootNode = (MappingNode)document;

        for(Map.Entry<String,Node> entry : rootNode.getValue().entrySet()) {
            String name = entry.getKey();
            Node node = entry.getValue();
            parseBeanDefinitions(name, node);
        }
    }

    protected BeanDefinition parseBeanDefinitions(String name, Node node) {
        BeanDefinitionParserResolver resolver = this.readerContext.getBeanDefinitionParserResolver();
        BeanDefinitionParser parser = resolver.resolveParser(node);
        if (parser == null) {
            String msg = "Unable to locate BeanDefinitionParser for YAML node [" + name + "]";
            this.readerContext.error(msg, node, parseState.snapshot());
            return null;
        }
        return parser.parse(node, new ParserContext(this.readerContext, null));
    }

}
