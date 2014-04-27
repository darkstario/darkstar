package io.darkstar.plugin;

import io.darkstar.config.Context;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class Directives {

    public static DirectivesBuilder builder() {
        return new DefaultDirectivesBuilder();
    }

    @SafeVarargs
    public static Set<Class<? extends Context>> setOf(Class<? extends Context>... contextClasses) {
        if (contextClasses == null) {
            return Collections.emptySet();
        }

        Set<Class<? extends Context>> classes = new LinkedHashSet<>(contextClasses.length);
        Collections.addAll(classes, contextClasses);
        return classes;
    }
}
