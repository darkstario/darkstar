package com.stormpath.monban.config;

public class VirtualHostConfig {

    private String name;
    private String description;
    private LogConfig log;
    private BalanceConfig balance;
    private StormpathConfig stormpath;

    public VirtualHostConfig(){}

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LogConfig getLog() {
        return log;
    }

    public BalanceConfig getBalance() {
        return balance;
    }

    public StormpathConfig getStormpath() {
        return stormpath;
    }
}
