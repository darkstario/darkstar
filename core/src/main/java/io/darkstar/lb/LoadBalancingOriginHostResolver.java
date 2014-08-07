package io.darkstar.lb;

import io.darkstar.http.gateway.OriginHostResolver;
import io.darkstar.http.Request;
import io.darkstar.net.Host;
import io.darkstar.net.HostParser;

public class LoadBalancingOriginHostResolver implements OriginHostResolver {

    @Override
    public Host getOriginHost(Request request) {
        //hard code for testing.  Logic TBD
        return HostParser.INSTANCE.parse("localhost:8080");
        //return new DefaultHost("google.com",80);
    }
}
