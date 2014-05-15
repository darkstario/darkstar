package io.darkstar.http;

import org.springframework.util.Assert;

public class DefaultVirtualHostResolver implements VirtualHostResolver {

    private static final char DOMAIN_PART_DELIMITER_CHAR = '.';
    private static final String WILDCARD_PREFIX = "*.";
    private static final String WILDCARD_SUFFIX = ".*";

    private final VirtualHostStore virtualHostStore;

    public DefaultVirtualHostResolver() {
        this(new DefaultVirtualHostStore());
    }

    public DefaultVirtualHostResolver(VirtualHostStore virtualHostStore) {
        Assert.notNull(virtualHostStore, "virtualHostStore cannot be null.");
        this.virtualHostStore = virtualHostStore;
    }

    @Override
    public VirtualHost getVirtualHost(final String requestHostName) throws UnknownVirtualHostException {
        Assert.hasText(requestHostName, "requestHostName is required.");

        VirtualHost virtualHost = this.virtualHostStore.findVirtualHost(requestHostName);
        if (virtualHost != null) {
            return virtualHost;
        }

        String domainName = requestHostName;
        int dotIndex = domainName.indexOf(DOMAIN_PART_DELIMITER_CHAR);

        if (dotIndex > 0) {
            domainName = domainName.substring(dotIndex + 1);
            virtualHost = this.virtualHostStore.findVirtualHost(WILDCARD_PREFIX + domainName);
            if (virtualHost != null) {
                return virtualHost;
            }
        }

        String msg = "Unable to locate Virtual Host for host name '" + requestHostName + "'";
        throw new UnknownVirtualHostException(msg);
    }

}
