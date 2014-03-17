package com.stormpath.monban.config.json;

public class SslConfig {

    private int port;
    private String certificateFile;
    private String certificateKeyFile;

    public SslConfig(){
        port = 443;
    }

    public int getPort() {
        return port;
    }

    public String getCertificateFile() {
        return certificateFile;
    }

    public String getCertificateKeyFile() {
        return certificateKeyFile;
    }
}
