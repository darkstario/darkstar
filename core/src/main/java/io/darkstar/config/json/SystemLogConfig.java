package io.darkstar.config.json;

public class SystemLogConfig extends LogConfig {

    protected String level;

    public SystemLogConfig() {
        this.path = "logs/error.log";
        this.format = "%date %-5level [%thread] %logger{36} %m%n";
        this.level = "warn";
    }

    public SystemLogConfig(SystemLogConfig src) {
        super(src);
        this.level = src.level;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
}
