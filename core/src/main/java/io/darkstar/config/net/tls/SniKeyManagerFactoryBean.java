package io.darkstar.config.net.tls;

import io.darkstar.tls.SniKeyManager;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.util.Assert;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.X509ExtendedKeyManager;

public class SniKeyManagerFactoryBean extends AbstractFactoryBean<SniKeyManager> {

    private KeyManagerFactory keyManagerFactory;

    public void setKeyManagerFactory(KeyManagerFactory keyManagerFactory) {
        this.keyManagerFactory = keyManagerFactory;
    }

    @Override
    public Class<?> getObjectType() {
        return SniKeyManager.class;
    }

    @Override
    protected SniKeyManager createInstance() throws Exception {

        Assert.notNull(keyManagerFactory, "keyManagerFactory is required.");

        // Javadoc of SSLContext.init() states the first KeyManager implementing X509ExtendedKeyManager in the array is
        // used. We duplicate this behaviour when picking the KeyManager to wrap.
        X509ExtendedKeyManager x509KeyManager = null;
        for (KeyManager keyManager : keyManagerFactory.getKeyManagers()) {
            if (keyManager instanceof X509ExtendedKeyManager) {
                x509KeyManager = (X509ExtendedKeyManager) keyManager;
                break;
            }
        }

        if (x509KeyManager == null) {
            String msg = "KeyManagerFactory did not return a (required) X509ExtendedKeyManager instance.";
            throw new BeanInitializationException(msg);
        }

        return new SniKeyManager(x509KeyManager);
    }
}
