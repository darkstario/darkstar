package io.darkstar.config.json;

import java.util.List;
import java.util.Map;

public class Config {

    private String name;
    private String host;
    private int port;
    private TlsConfig tls;
    private List<VirtualHostConfig> vhosts;

    private Object systemLog; //string or map of props
    private Object accessLog; //string or map of props

    public Config() {
        host = "0.0.0.0"; //bind any/all addresses by default
        port = 80;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public TlsConfig getTls() {
        return tls;
    }

    public List<VirtualHostConfig> getVhosts() {
        return vhosts;
    }

    public Object getSystemLog() {
        return systemLog;
    }

    public Object getAccessLog() {
        return accessLog;
    }
}
