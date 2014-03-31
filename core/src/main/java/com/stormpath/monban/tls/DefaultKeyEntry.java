package com.stormpath.monban.tls;

import org.springframework.util.Assert;

import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.List;

public class DefaultKeyEntry implements KeyEntry {

    private final PrivateKey privateKey;
    private final Certificate[] certificateChain;

    public DefaultKeyEntry(PrivateKey privateKey, List<Certificate> certificateChain) {
        Assert.notNull(privateKey, "PrivateKey is required.");
        Assert.notEmpty(certificateChain, "Certificate chain cannot be null or empty.");
        this.privateKey = privateKey;
        this.certificateChain = certificateChain.toArray(new Certificate[certificateChain.size()]);
    }

    @Override
    public PrivateKey getPrivateKey() {
        return this.privateKey;
    }

    @Override
    public Certificate[] getCertificateChain() {
        return this.certificateChain;
    }
}
