package io.darkstar.tls;

import io.darkstar.config.json.TlsConfig;

import java.io.IOException;
import java.security.cert.CertificateException;

public interface KeyEntryFactory {

    KeyEntry createKeyEntry(String serverName, TlsConfig config) throws IOException, CertificateException;

}
