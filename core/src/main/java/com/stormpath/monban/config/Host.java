package com.stormpath.monban.config;

public class Host {

    private final String name;
    private final int port;

    public Host(String name, int port) {
        this.name = name;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        if (port < 1) {
            return name;
        }
        return name + ":" + port;
    }
}
