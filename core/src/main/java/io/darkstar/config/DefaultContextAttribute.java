package io.darkstar.config;

import org.springframework.util.Assert;

public class DefaultContextAttribute<T extends Context> implements ContextAttribute<T> {

    private final String name;
    private final Object value;
    private final T context;

    public DefaultContextAttribute(String name, Object value, T context) {
        Assert.hasText(name, "name argument cannot be null or empty.");
        Assert.notNull(value, "value argument is required.");
        Assert.notNull(context, "context argument is required.");
        this.name = name;
        this.value = value;
        this.context = context;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public T getContext() {
        return context;
    }
}
