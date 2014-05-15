package io.darkstar.http;

import io.darkstar.net.Host;

import java.util.Set;

public interface VirtualHost {

    String getName();

    Set<String> getAliases();

    //THIS IS TEMPORARY: FOR TESTING ONLY
    Host getOriginHost();
}
