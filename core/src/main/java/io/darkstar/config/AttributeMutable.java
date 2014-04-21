package io.darkstar.config;

public interface AttributeMutable {

    <T> T setAttribute(String name, T value);
}
