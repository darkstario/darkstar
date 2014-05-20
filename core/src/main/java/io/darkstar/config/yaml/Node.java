package io.darkstar.config.yaml;

public interface Node<T> {

    String getName();

    String getTag();

    boolean hasParent();

    Node getParent();

    T getValue();
}
