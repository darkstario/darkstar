package org.springframework.beans.factory.yaml;

import org.springframework.util.Assert;

public abstract class AbstractNode<T> implements Node<T> {

    private Node parent;
    private final String name;

    public AbstractNode(Node parent, String name) {
        Assert.hasText(name, "name argument must be a populated String value.");
        this.name = name;
        this.parent = parent;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean hasParent() {
        return parent != null;
    }

    @Override
    public Node getParent() {
        return parent;
    }
}
