package io.darkstar.config.yaml;

public interface AttributeMutable {

    <T> T setAttribute(String name, T value);
}
