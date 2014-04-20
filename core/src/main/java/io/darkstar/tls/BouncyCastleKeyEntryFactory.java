package io.darkstar.tls;

import io.darkstar.config.json.TlsConfig;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class BouncyCastleKeyEntryFactory implements KeyEntryFactory {

    private final JcaX509CertificateConverter x509Converter = new JcaX509CertificateConverter();

    private final JcaPEMKeyConverter pemKeyConverter = new JcaPEMKeyConverter();

    public BouncyCastleKeyEntryFactory() {
    }

    private static File assertFile(String serverName, File file, boolean cert) {
        if (!file.exists()) {
            String msg = serverName + " tls " + (cert ? "cert" : "key") + " file " + file + " does not exist.";
            throw new IllegalArgumentException(msg);
        }
        if (file.isDirectory()) {
            String msg = serverName + " tls " + (cert ? "cert" : "key") + " file " + file + " is a directory, not a file as expected.";
            throw new IllegalArgumentException(msg);
        }
        if (!file.canRead()) {
            String msg = "Cannot read " + serverName + " tls " + (cert ? "cert" : "key") + " file " + file + ": check file permissions.";
            throw new IllegalArgumentException(msg);
        }
        return file;
    }

    private List<Certificate> readCerts(Resource resource) throws CertificateException, IOException {

        try (Reader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), "UTF-8"))) {

            PEMParser pemParser = new PEMParser(reader);

            Object o;

            List<Certificate> certs = new ArrayList<>();

            while ((o = pemParser.readObject()) != null) {
                if (!(o instanceof X509CertificateHolder)) {
                    String msg = "cert file " + resource.getDescription() + " contains a resource that is not an " +
                            "X509 certificate.";
                    throw new IllegalArgumentException(msg);
                }
                X509CertificateHolder holder = (X509CertificateHolder) o;
                X509Certificate cert = x509Converter.getCertificate(holder);
                certs.add(cert);
            }

            if (certs.isEmpty()) {
                String msg = "Unable to find any public certificates in cert file " + resource.getDescription();
                throw new IllegalArgumentException(msg);
            }

            return certs;
        }
    }

    private PrivateKey readPrivateKey(Resource resource) throws IOException {

        try (Reader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), "UTF-8"))) {

            PEMParser pemParser = new PEMParser(reader);

            PrivateKey privateKey;

            Object o = pemParser.readObject();

            if (o == null) {
                throw new IllegalArgumentException("Private key was not found in key file " + resource.getDescription());
            }

            if (o instanceof PEMKeyPair) {
                PEMKeyPair pemKeyPair = (PEMKeyPair) o;
                privateKey = pemKeyConverter.getPrivateKey(pemKeyPair.getPrivateKeyInfo());
            } else if (o instanceof PrivateKeyInfo) {
                PrivateKeyInfo privateKeyInfo = (PrivateKeyInfo) o;
                privateKey = pemKeyConverter.getPrivateKey(privateKeyInfo);
            } else if (o instanceof PKCS8EncryptedPrivateKeyInfo) {
                String msg = "PKCS8 Encrypted PrivateKeys are not supported. Only unencrypted private keys.";
                throw new IllegalArgumentException(msg);
            } else {
                String msg = "key file contains a resource that is not a private key.";
                throw new IllegalArgumentException(msg);
            }

            return privateKey;
        }
    }

    @Override
    public KeyEntry createKeyEntry(String serverName, TlsConfig config) throws IOException, CertificateException {

        Assert.notNull(config, "tls config cannot be null.");

        String path = config.getCert();
        Assert.hasText(path, serverName + " tls 'cert' file path is not specified.");
        File certFile = assertFile(serverName, new File(path), true);
        Resource certFileResource = new FileSystemResource(certFile);

        path = config.getKey();
        Assert.hasText(path, serverName + " tls 'key' file path is not specified.");
        File keyFile = assertFile(serverName, new File(path), false);
        Resource keyFileResource = new FileSystemResource(keyFile);

        return createKeyEntry(certFileResource, keyFileResource);
    }

    @Override
    public KeyEntry createKeyEntry(Resource pemEncodedCert, Resource pemEncodedPrivateKey) throws IOException, CertificateException {

        List<Certificate> certs = readCerts(pemEncodedCert);

        PrivateKey privateKey = readPrivateKey(pemEncodedPrivateKey);

        return new DefaultKeyEntry(privateKey, certs);
    }
}
