package io.darkstar.config.yaml;

import com.stormpath.sdk.lang.Assert;

public class DefaultScalarNode extends AbstractNode<Object> implements ScalarNode {

    private final Object value;

    public DefaultScalarNode(Node parent, String name, Object value) {
        super(parent, name);
        Assert.notNull(value, "value argument cannot be null.");
        this.value = value;
    }

    @Override
    public Object getValue() {
        return this.value;
    }
}
