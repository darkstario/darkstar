package io.darkstar.http;

import io.darkstar.net.Host;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Set;

public class DefaultVirtualHost implements VirtualHost {

    private final String name;
    private final Set<String> aliases;
    private final Host originHost;

    public DefaultVirtualHost(String name, Set<String> aliases, Host originHost) {
        this.name = name;
        this.aliases = CollectionUtils.isEmpty(aliases) ? Collections.emptySet() : Collections.unmodifiableSet(aliases);
        this.originHost = originHost;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Set<String> getAliases() {
        return this.aliases;
    }

    @Override
    public Host getOriginHost() {
        return this.originHost;
    }
}
