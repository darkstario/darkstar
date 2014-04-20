package io.darkstar.security;

import io.darkstar.tls.BouncyCastleKeyEntryFactory;
import io.darkstar.tls.KeyEntryFactory;
import io.darkstar.tls.SniKeyManager;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509ExtendedKeyManager;
import java.security.KeyStore;

@Configuration
public class SecurityConfig {

    private static final char[] KEY_STORE_PASSWORD = "changeit".toCharArray();

    @Bean
    public KeyStore keyStore() {
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(null);
            return ks;
        } catch (Exception e) {
            throw new BeanCreationException("Unable to initialize KeyStore.", e);
        }
    }

    @Bean
    public SSLContext sslContext() {
        try {
            KeyStore keyStore = keyStore();

            KeyManagerFactory factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            factory.init(keyStore, KEY_STORE_PASSWORD);

            // Javadoc of SSLContext.init() states the first KeyManager implementing X509ExtendedKeyManager in the array is
            // used. We duplicate this behaviour when picking the KeyManager to wrap around.
            X509ExtendedKeyManager x509KeyManager = null;
            for (KeyManager keyManager : factory.getKeyManagers()) {
                if (keyManager instanceof X509ExtendedKeyManager) {
                    x509KeyManager = (X509ExtendedKeyManager) keyManager;
                }
            }

            if (x509KeyManager == null) {
                throw new Exception("KeyManagerFactory did not create an X509ExtendedKeyManager");
            }

            SniKeyManager sniKeyManager = new SniKeyManager(x509KeyManager);

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(new KeyManager[]{sniKeyManager}, null, null);

            return context;
        } catch (Exception e) {
            throw new BeanCreationException("Unable to create SSLContext.", e);
        }
    }

    @Bean
    public KeyStoreManager keyStoreManager() {
        KeyEntryFactory keyEntryFactory = new BouncyCastleKeyEntryFactory();
        return new DefaultKeyStoreManager(keyStore(), KEY_STORE_PASSWORD, keyEntryFactory);
    }
}
