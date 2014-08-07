package org.springframework.beans.factory.yaml;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.ComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.util.Stack;

/**
 * Context that gets passed along a bean definition parsing process, encapsulating all relevant configuration as well
 * as state. Nested inside a {@link YamlReaderContext}.
 *
 * @since 0.1
 * @see YamlReaderContext
 */
public final class ParserContext {

    private final YamlReaderContext readerContext;

    //private final BeanDefinitionParserDelegate delegate;

    private BeanDefinition containingBeanDefinition;

    private final Stack<ComponentDefinition> containingComponents = new Stack<>();

    public ParserContext(YamlReaderContext readerContext /*, BeanDefinitionParserDelegate delegate*/) {
        this.readerContext = readerContext;
        //this.delegate = delegate;
    }

    public ParserContext(YamlReaderContext readerContext, /*, BeanDefinitionParserDelegate delegate,*/
                         BeanDefinition containingBeanDefinition) {

        this.readerContext = readerContext;
        //this.delegate = delegate;
        this.containingBeanDefinition = containingBeanDefinition;
    }


    public final YamlReaderContext getReaderContext() {
        return this.readerContext;
    }

    public final BeanDefinitionRegistry getRegistry() {
        return this.readerContext.getRegistry();
    }

    /*
    public final BeanDefinitionParserDelegate getDelegate() {
        return this.delegate;
    }
    */

    public final BeanDefinition getContainingBeanDefinition() {
        return this.containingBeanDefinition;
    }

    public final boolean isNested() {
        return (this.containingBeanDefinition != null);
    }


    public boolean isDefaultLazyInit() {
        return true;
        //return BeanDefinitionParserDelegate.TRUE_VALUE.equals(this.delegate.getDefaults().getLazyInit());
    }

    public Object extractSource(Object sourceCandidate) {
        return this.readerContext.extractSource(sourceCandidate);
    }

    public CompositeComponentDefinition getContainingComponent() {
        return (!this.containingComponents.isEmpty() ?
                (CompositeComponentDefinition) this.containingComponents.lastElement() : null);
    }

    public void pushContainingComponent(CompositeComponentDefinition containingComponent) {
        this.containingComponents.push(containingComponent);
    }

    public CompositeComponentDefinition popContainingComponent() {
        return (CompositeComponentDefinition) this.containingComponents.pop();
    }

    public void popAndRegisterContainingComponent() {
        registerComponent(popContainingComponent());
    }

    public void registerComponent(ComponentDefinition component) {
        CompositeComponentDefinition containingComponent = getContainingComponent();
        if (containingComponent != null) {
            containingComponent.addNestedComponent(component);
        }
        else {
            this.readerContext.fireComponentRegistered(component);
        }
    }

    public void registerBeanComponent(BeanComponentDefinition component) {
        BeanDefinitionReaderUtils.registerBeanDefinition(component, getRegistry());
        registerComponent(component);
    }
}
