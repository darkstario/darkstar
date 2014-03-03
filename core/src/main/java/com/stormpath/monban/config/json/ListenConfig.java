package com.stormpath.monban.config.json;

public class ListenConfig {

    private String iface;
    private int port;
    private int sslPort;

    public ListenConfig(){}

    public String getIface() {
        return iface;
    }

    public int getPort() {
        return port;
    }

    public int getSslPort() {
        return sslPort;
    }
}
