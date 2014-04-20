package io.darkstar.tls;

import io.darkstar.config.json.TlsConfig;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.security.cert.CertificateException;

public interface KeyEntryFactory {

    KeyEntry createKeyEntry(String serverName, TlsConfig config) throws IOException, CertificateException;

    KeyEntry createKeyEntry(Resource pemEncodedCert, Resource pemEncodedPrivateKey) throws IOException, CertificateException;

}
