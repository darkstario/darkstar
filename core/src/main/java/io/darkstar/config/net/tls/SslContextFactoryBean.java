package io.darkstar.config.net.tls;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import java.util.List;

public class SslContextFactoryBean extends AbstractFactoryBean<SSLContext> {

    private List<KeyManager> keyManagers;

    protected String protocol = "TLS";
    protected String providerName;

    public void setKeyManagers(List<KeyManager> keyManagers) {
        Assert.notNull(keyManagers, "KeyManager list cannot be null.");
        this.keyManagers = keyManagers;
    }

    public void setProtocol(String protocol) {
        Assert.hasText(protocol, "protocol name cannot be null or empty.");
        this.protocol = protocol;
    }

    public void setProviderName(String providerName) {
        Assert.hasText(providerName, "provider name cannot be null or empty.");
        this.providerName = providerName;
    }

    @Override
    public Class<?> getObjectType() {
        return SSLContext.class;
    }

    @Override
    protected SSLContext createInstance() throws Exception {

        SSLContext context;

        if (StringUtils.hasText(this.providerName)) {
            context = SSLContext.getInstance(protocol, providerName);
        } else {
            context = SSLContext.getInstance(protocol);
        }

        Assert.notEmpty(keyManagers, "keyManagers list cannot be null or empty.");

        KeyManager[] keyManagersArray = new KeyManager[keyManagers.size()];
        keyManagers.toArray(keyManagersArray);

        context.init(keyManagersArray, null, null);

        return context;
    }
}
