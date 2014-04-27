package io.darkstar.plugin;

import io.darkstar.config.Context;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Set;

/**
 * Default {@link Directive} implementation.  This implementation is immutable (and therefore thread-safe).
 */
public class DefaultDirective implements Directive {

    private final String name;
    private final Set<Class<? extends Context>> contextClasses;

    public DefaultDirective(String name, Set<Class<? extends Context>> contextClasses) {
        Assert.hasText(name, "name argument cannot be null or empty.");
        Assert.notEmpty(contextClasses, "contextClasses cannot be null or empty.");
        this.name = name;
        this.contextClasses = Collections.unmodifiableSet(contextClasses);
    }

    @Override
    public boolean supports(Context context) {
        if (context == null) {
            return false;
        }
        for (Class<? extends Context> clazz : contextClasses) {
            if (clazz.isInstance(context)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultDirective)) return false;

        DefaultDirective that = (DefaultDirective) o;

        return name.equals(that.name) &&
                contextClasses.equals(that.contextClasses);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + contextClasses.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DefaultDirective{" +
                "name='" + name + '\'' +
                ", contextClasses=" + contextClasses +
                '}';
    }
}
