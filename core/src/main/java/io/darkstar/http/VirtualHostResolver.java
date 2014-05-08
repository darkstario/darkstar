package io.darkstar.http;

public interface VirtualHostResolver {

    VirtualHost getVirtualHost(String requestedHostName);
}
