package io.darkstar.net;

public interface Connector {

    /**
     * Returns the specified {@link java.net.InetSocketAddress} to bind to when creating a Server Channel (socket), or
     * {@code null} if none was specified and the system should bind to all addresses (for example, {@code 0.0.0.0}).
     *
     * @return the specified {@link java.net.InetSocketAddress} to bind to when creating a Server Channel (socket), or
     *         {@code null} if none was specified and the system should bind to all addresses (for example,
     *         {@code 0.0.0.0}).
     */
    String getAddress();

    /**
     * Returns the specified port to bind to when creating a Server Channel (socket) or a non-positive value
     * (0, -1, etc) if the system should automatically bind to a default port based on the process's Effective User
     * privileges (for example, when running with an Effective UID of root, port 80, or when a normal user, 8000).
     *
     * @return the specified port to bind to when creating a Server Channel (socket) or a non-positive value
     *         (0, -1, etc) if the system should automatically bind to a default port based on the process's
     *         Effective User privileges (for example, when running with an Effective UID of root, port 80, or
     *         when a normal user, 8000).
     */
    int getPort();

}
