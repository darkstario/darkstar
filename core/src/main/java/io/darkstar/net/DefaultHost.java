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
    public boolean hasPort() {
        return this.port > 0;
    }

    @Override
    public int getPort() {
        if (!hasPort()) {
            throw new IllegalStateException("No port value specified.");
        }
        return this.port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultHost)) return false;

        DefaultHost that = (DefaultHost) o;

        return port == that.port &&
                (name != null ? name.equals(that.name) : that.name == null);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name != null ? name : '*');

        if (hasPort()) {
            sb.append(':').append(port);
        }

        return sb.toString();
    }
}
