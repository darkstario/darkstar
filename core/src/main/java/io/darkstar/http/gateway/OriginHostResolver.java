package io.darkstar.http.gateway;

import io.darkstar.http.Request;
import io.darkstar.net.Host;

public interface OriginHostResolver {

    Host getOriginHost(Request request);
}
