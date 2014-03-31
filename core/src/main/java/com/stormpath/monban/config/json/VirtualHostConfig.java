package com.stormpath.monban.config.json;

public class VirtualHostConfig {

    private String name;
    private String description;
    private TlsConfig tls;
    private LogConfig log;
    private BalanceConfig balance;
    private StormpathConfig stormpath;
    private DucksboardConfig ducksboard;
    private DatadogConfig datadog;

    public VirtualHostConfig(){}

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public TlsConfig getTls() {
        return tls;
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

    public DucksboardConfig getDucksboard() {
        return ducksboard;
    }

    public DatadogConfig getDatadog() {
        return datadog;
    }
}
