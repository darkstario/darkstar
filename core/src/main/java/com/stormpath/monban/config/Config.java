package com.stormpath.monban.config;

import java.util.List;

public class Config {

    private String name;
    private String description;
    private ListenConfig listen;
    private List<VirtualHostConfig> vhosts;

    public Config(){}

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ListenConfig getListen() {
        return listen;
    }

    public List<VirtualHostConfig> getVhosts() {
        return vhosts;
    }
}
