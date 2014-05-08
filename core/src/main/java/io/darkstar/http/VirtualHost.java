package io.darkstar.http;

import java.util.Set;

public interface VirtualHost {

    String getName();

    Set<String> getAliases();
}
