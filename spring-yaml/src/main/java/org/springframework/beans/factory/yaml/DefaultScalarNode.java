package org.springframework.beans.factory.yaml;

import org.springframework.util.Assert;

public class DefaultScalarNode extends AbstractNode<Object> implements ScalarNode {

    private final Object value;

    private final org.yaml.snakeyaml.nodes.ScalarNode source;

    public DefaultScalarNode(Node parent, String name, Object value) {
        super(parent, name);
        Assert.notNull(value, "value argument cannot be null.");
        this.value = value;
        this.source = null;
    }

    public DefaultScalarNode(Node parent, String name, org.yaml.snakeyaml.nodes.ScalarNode source) {
        super(parent, name);
        Assert.notNull(source.getValue(), "value argument cannot be null.");
        this.value = source.getValue();
        this.source = source;
    }

    @Override
    public Object getValue() {
        return this.value;
    }

    @Override
    public String getTag() {
        return getSource().getTag().getValue();
    }

    @Override
    public org.yaml.snakeyaml.nodes.ScalarNode getSource() {
        return this.source;
    }
}
