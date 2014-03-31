package com.stormpath.monban.tls;

import java.security.PrivateKey;
import java.security.cert.Certificate;

public interface KeyEntry {

    PrivateKey getPrivateKey();

    Certificate[] getCertificateChain();
}
