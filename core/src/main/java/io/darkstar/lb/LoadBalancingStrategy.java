package io.darkstar.lb;

import io.darkstar.http.Request;
import io.darkstar.http.VirtualHost;
import io.darkstar.net.Host;

public interface LoadBalancingStrategy {

    Host getDestination(Request request, VirtualHost vhost);
}
