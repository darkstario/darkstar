package io.darkstar.plugin;

import io.darkstar.config.Context;
import io.darkstar.config.IdentifierName;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class DefaultDirectivesBuilder implements DirectivesBuilder {

    private Map<String, Directive> directives;

    public DefaultDirectivesBuilder() {
        this.directives = new LinkedHashMap<>();
    }

    @Override
    public DirectivesBuilder add(String name, Class<? extends Context> contextClass) {
        return this.add(name, Directives.setOf(contextClass));
    }

    @Override
    public DirectivesBuilder add(String name, Set<Class<? extends Context>> contextClasses) {
        name = IdentifierName.of(name);
        Directive directive = new DefaultDirective(name, contextClasses);
        directives.put(name, directive);
        return this;
    }

    @Override
    public Map<String, Directive> buildMap() {
        return Collections.unmodifiableMap(directives);
    }
}
