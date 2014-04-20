package io.darkstar.security;

import io.darkstar.tls.KeyEntry;
import io.darkstar.tls.KeyEntryFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.security.KeyStore;

public class DefaultKeyStoreManager implements KeyStoreManager {

    private final KeyStore keyStore;
    private final char[] keyPassword;
    private final KeyEntryFactory keyEntryFactory;

    public DefaultKeyStoreManager(KeyStore keyStore, char[] keyPassword, KeyEntryFactory keyEntryFactory) {
        Assert.notNull(keyStore, "keyStore is required.");
        Assert.isTrue(keyPassword != null && keyPassword.length > 0, "keyPassword is required.");
        Assert.notNull(keyEntryFactory, "keyEntryFactory is required.");
        this.keyStore = keyStore;
        this.keyPassword = keyPassword;
        this.keyEntryFactory = keyEntryFactory;
    }


    @Override
    public void addEntry(String hostName, Resource pemEncodedCertChain, Resource pemEncodedPrivateKey) {
        try {
            KeyEntry keyEntry = keyEntryFactory.createKeyEntry(pemEncodedCertChain, pemEncodedPrivateKey);
            this.keyStore.setKeyEntry(hostName, keyEntry.getPrivateKey(), this.keyPassword, keyEntry.getCertificateChain());
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to add key entry.", e);
        }
    }
}
