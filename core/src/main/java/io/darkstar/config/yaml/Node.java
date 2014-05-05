package io.darkstar.config.yaml;

public interface Node<T> {

    String getName();

    boolean hasParent();

    Node getParent();

    T getValue();
}
