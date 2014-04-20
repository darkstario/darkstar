package io.darkstar.config;

public interface Config {

    <T> T getValue(String expression);

    <T> T getValue(String expression, Class<T> expectedType);
}
