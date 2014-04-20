package io.darkstar.security;

import org.springframework.core.io.Resource;

public interface KeyStoreManager {

    void addEntry(String hostName, Resource pemEncodedCertChain, Resource pemEncodedPrivateKey);

}
