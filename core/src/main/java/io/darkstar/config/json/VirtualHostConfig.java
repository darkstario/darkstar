package io.darkstar.config.json;

import java.util.Map;

public class VirtualHostConfig {

    private String name;
    private String description;
    private TlsConfig tls;
    private Map<String,Object> log;
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

    public Map<String, Object> getLog() {
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
