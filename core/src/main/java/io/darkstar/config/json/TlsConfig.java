package io.darkstar.config.json;

public class TlsConfig {

    private int port;
    private String cert;
    private String key;

    public TlsConfig() {
        port = 443;
    }

    public int getPort() {
        return port;
    }

    public String getCert() {
        return cert;
    }

    public String getKey() {
        return key;
    }
}
