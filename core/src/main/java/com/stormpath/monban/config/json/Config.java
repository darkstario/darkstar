package com.stormpath.monban.config.json;

import java.util.List;

public class Config {

    private String name;
    private String host;
    private int port;
    private SslConfig ssl;
    private List<VirtualHostConfig> vhosts;

    public Config() {
        host = "0.0.0.0"; //bind any/all addresses by default
        port = 80;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public SslConfig getSsl() {
        return ssl;
    }

    public List<VirtualHostConfig> getVhosts() {
        return vhosts;
    }
}
