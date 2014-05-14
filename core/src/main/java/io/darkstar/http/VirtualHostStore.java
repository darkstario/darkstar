package io.darkstar.http;

public interface VirtualHostStore {

    VirtualHost getVirtualHost(String hostName);
}
