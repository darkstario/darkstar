package io.darkstar.http;

public interface VirtualHostStore {

    VirtualHost findVirtualHost(String hostName);
}
