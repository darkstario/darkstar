package io.darkstar.config.json;

public interface LogConfigFactory<T extends LogConfig> {

    T newInstance(Object config, T parent);
}
