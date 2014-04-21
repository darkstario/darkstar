package io.darkstar.config;

import org.springframework.util.Assert;

import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class DefaultContext<C extends Context> implements Context<C>, AttributeMutable {

    private final String name;
    private final C parent;
    private final Map<String, Object> attributes;

    protected DefaultContext(String name, C parent) {
        Assert.hasText(name, "name is required");
        this.name = name;
        this.parent = parent;
        this.attributes = new LinkedHashMap<>();
    }

    @Override
    public final String getName() {
        return this.name;
    }

    @Override
    public C getParent() {
        return this.parent;
    }

    @Override
    public <T> T getAttribute(String name) {
        return (T) this.attributes.get(name);
    }

    @Override
    public <T> T setAttribute(String name, T value) {
        return (T) this.attributes.put(name, value);
    }

    public void setAttributes(Map<String,Object> attributes) {
        this.attributes.clear();
        this.attributes.putAll(attributes);
    }

    public void putAttributes(Map<String,Object> attributes) {
        this.attributes.putAll(attributes);
    }

    public Map<String,Object> getAttributes() {
        return this.attributes;
    }

    @Override
    public String toString() {
        Context parent = getParent();
        if (parent != null) {
            return parent.toString() + "/" + getName();
        }
        return getName();
    }
}
