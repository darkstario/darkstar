package io.darkstar.http;

import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Set;

public class DefaultVirtualHost implements VirtualHost {

    private final String name;
    private final Set<String> aliases;

    public DefaultVirtualHost(String name, Set<String> aliases) {
        this.name = name;
        this.aliases = CollectionUtils.isEmpty(aliases) ? Collections.emptySet() : Collections.unmodifiableSet(aliases);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Set<String> getAliases() {
        return this.aliases;
    }
}
