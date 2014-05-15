package io.darkstar.net;

import org.springframework.util.Assert;

public class DefaultHostParser implements HostParser {

    @Override
    public Host parse(String value) {
        return parse(value, -1);
    }

    @Override
    public Host parse(String value, int defaultPort) {
        Assert.hasText(value, "value argument must contain text.");

        int colonIndex = value.lastIndexOf(':');
        if (colonIndex > 0) {
            String address = value.substring(0, colonIndex);
            String portString = value.substring(colonIndex + 1);
            int port = PortParser.INSTANCE.parsePort(portString);
            return new DefaultHost(address, port);
        }

        //otherwise, no colon char.  Let's see if what they specified was only a port or only an address:
        int port = defaultPort;
        try {
            port = PortParser.INSTANCE.parsePort(value);
            //it is a port, which means they didn't specify an address. Return the value:
            return new DefaultHost(null, port);
        } catch (IllegalArgumentException ignored) {
            //not a port, just keep our default
            return new DefaultHost(value, port);
        }
    }
}
