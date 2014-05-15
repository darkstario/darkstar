package io.darkstar.http;

import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultVirtualHostStore implements ConcurrentVirtualHostStore {

    private final ConcurrentMap<String,VirtualHost> vhosts;

    public DefaultVirtualHostStore() {
        this(new ConcurrentHashMap<>());
    }

    public DefaultVirtualHostStore(ConcurrentMap<String, VirtualHost> map) {
        Assert.notNull(map, "map argument cannot be null.");
        this.vhosts = map;
    }

    @Override
    public VirtualHost findVirtualHost(String hostName) {
        return this.vhosts.get(hostName);
    }

    @Override
    public VirtualHost putIfAbsent(VirtualHost vhost) {

        Assert.notNull(vhost, "VirtualHost cannot be null.");

        String serverName = vhost.getName();

        Assert.hasText(serverName, "VirtualHost name is required.");

        //todo: assert valid domain name

        VirtualHost existing = this.vhosts.putIfAbsent(serverName, vhost);

        if (existing == null) {
            //register any aliases as well:
            Set<String> aliases = vhost.getAliases();

            if (!CollectionUtils.isEmpty(aliases)) {

                for(String alias : aliases) {
                    //todo: assert valid domain name:
                    this.vhosts.putIfAbsent(alias, vhost);
                }
            }
        }

        return existing;
    }
}
