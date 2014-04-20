package io.darkstar.config;

public interface Node {

    String getName();

    boolean hasParent();

    Node getParent();

    <T> T getValue();

    <T> T getValue(String expr);
}
