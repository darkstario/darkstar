package io.darkstar.config.net.tls;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.util.StringUtils;

import java.security.KeyStore;

public class MemoryKeyStoreFactoryBean extends AbstractFactoryBean<KeyStore> {

    private String type;
    private String providerName;

    @Override
    public Class<?> getObjectType() {
        return KeyStore.class;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    @Override
    protected KeyStore createInstance() throws Exception {

        String type = this.type;
        if (!StringUtils.hasText(type)) {
            type = KeyStore.getDefaultType();
        }

        KeyStore ks;

        if (StringUtils.hasText(providerName)) {
            ks = KeyStore.getInstance(type, providerName);
        } else {
            ks = KeyStore.getInstance(type);
        }

        ks.load(null); //loading without params makes it a memory-only store (go figure!).

        return ks;
    }
}
