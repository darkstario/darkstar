package io.darkstar.config.json;

public class VirtualHostConfig {

    private String name;
    private String description;
    private TlsConfig tls;
    private Object accessLog;
    private Object systemLog;
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

    public Object getAccessLog() {
        return accessLog;
    }

    public Object getSystemLog() {
        return systemLog;
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
