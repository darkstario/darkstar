package io.darkstar.config;

public interface ContextAttribute<T extends Context> {

    String getName();

    Object getValue();

    T getContext();

}
