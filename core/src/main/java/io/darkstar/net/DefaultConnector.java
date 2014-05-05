package io.darkstar.net;

public class DefaultConnector implements Connector {

    private final String address;
    private final int port;

    public DefaultConnector() {
        this.address = null;
        this.port = -1;
    }

    public DefaultConnector(String address, int port) {
        this.address = address;
        this.port = port;
    }

    @Override
    public String getAddress() {
        return this.address;
    }

    @Override
    public int getPort() {
        return this.port;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if ( address != null) {
            sb.append(address);
            if (port > 0) {
                sb.append(':');
            }
        }
        if (port > 0) {
            sb.append(port);
        }

        return sb.toString();
    }
}
