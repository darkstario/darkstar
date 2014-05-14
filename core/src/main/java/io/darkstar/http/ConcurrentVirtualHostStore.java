package io.darkstar.http;

public interface ConcurrentVirtualHostStore extends VirtualHostStore {

    VirtualHost putIfAbsent(VirtualHost virtualHost);
}
