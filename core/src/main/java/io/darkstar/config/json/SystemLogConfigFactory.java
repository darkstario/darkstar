package io.darkstar.config.json;

import java.util.Map;

public class SystemLogConfigFactory implements LogConfigFactory<SystemLogConfig> {

    @Override
    public SystemLogConfig newInstance(Object o, SystemLogConfig parent) {
        SystemLogConfig config = parent != null ? new SystemLogConfig(parent) : new SystemLogConfig();

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
            if (options.containsKey("level")) {
                config.setLevel(String.valueOf(options.get("level")));
            }
        } else {
            throw new IllegalArgumentException("Invalid systemLog config: " + o);
        }

        return config;
    }

}
