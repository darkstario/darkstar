package io.darkstar.net;

public class DefaultHost implements Host {

    private final String name;
    private final int port;

    public DefaultHost(String name, int port) {
        this.name = name;
        this.port = port;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getPort() {
        return this.port;
    }
}
