package io.darkstar.config.json;

import java.util.Map;

public class DefaultLogConfigFactory implements LogConfigFactory<LogConfig> {

    @Override
    public LogConfig newInstance(Object o, LogConfig parent) {

        LogConfig config = instantiate(parent);

        if (o instanceof String) {
            config.setPath(String.valueOf(o));
        } else if (o instanceof Map) {
            Map options = (Map) o;
            if (options.containsKey("path")) {
                config.setPath(String.valueOf(options.get("path")));
            }
            if (options.containsKey("format")) {
                config.setFormat(String.valueOf(options.get("format")));
            }
        } else {
            throw new IllegalArgumentException("Invalid log config: " + o);
        }

        return config;
    }

    protected LogConfig instantiate(LogConfig parent) {
        return parent != null ? new LogConfig(parent) : new LogConfig();
    }
}
