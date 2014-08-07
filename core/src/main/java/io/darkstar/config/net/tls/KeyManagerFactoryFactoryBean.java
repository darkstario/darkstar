package io.darkstar.config.net.tls;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.net.ssl.KeyManagerFactory;
import java.security.KeyStore;

public class KeyManagerFactoryFactoryBean extends AbstractFactoryBean<KeyManagerFactory> {

    private String algorithm;
    private String providerName;

    private KeyStore keyStore;
    private char[] keyStorePassword;

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public void setKeyStore(KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    public void setKeyStorePassword(char[] keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    @Override
    public Class<?> getObjectType() {
        return KeyManagerFactory.class;
    }

    @Override
    protected KeyManagerFactory createInstance() throws Exception {

        Assert.notNull(keyStore, "keyStore is required.");
        Assert.isTrue(keyStorePassword != null && keyStorePassword.length > 0, "keyStore password is required.");

        KeyManagerFactory factory;

        String algorithm = this.algorithm;
        if (!StringUtils.hasText(algorithm)) {
            algorithm = KeyManagerFactory.getDefaultAlgorithm();
        }

        if (StringUtils.hasText(providerName)) {
            factory = KeyManagerFactory.getInstance(algorithm, providerName);
        } else {
            factory = KeyManagerFactory.getInstance(algorithm);
        }

        factory.init(keyStore, keyStorePassword);

        return factory;
    }
}
