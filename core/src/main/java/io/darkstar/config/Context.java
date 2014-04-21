package io.darkstar.config;

public interface Context<C extends Context> {

    String getName();

    C getParent();

    <T> T getAttribute(String name);

}
