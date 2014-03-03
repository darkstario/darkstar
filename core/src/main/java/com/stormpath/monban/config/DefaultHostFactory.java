package com.stormpath.monban.config;

import org.springframework.stereotype.Component;

@Component("hostFactory")
public class DefaultHostFactory implements HostFactory {

    private static final int DEFAULT_PORT = 80;

    @Override
    public Host getHost(String s) {

        String name = s;
        int port = DEFAULT_PORT;

        int colonIndex = s.lastIndexOf(':');

        if (colonIndex > 0) {
            String portString = name.substring(colonIndex + 1);
            name = name.substring(0, colonIndex);
            port = Integer.parseInt(portString);
        }

        return new Host(name, port);
    }
}
