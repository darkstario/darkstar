package io.darkstar.config;

public interface Context<C extends Context> {

    String name();

    C getParent();

    <T> T getAttribute(String name);

}
