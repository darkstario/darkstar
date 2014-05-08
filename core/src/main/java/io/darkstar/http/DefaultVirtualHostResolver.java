package io.darkstar.http;

public class DefaultVirtualHostResolver implements VirtualHostResolver {

    @Override
    public VirtualHost getVirtualHost(String requestedHostName) {
        //TODO: implement:
        return new DefaultVirtualHost("localhost:8080", null);
    }
}
