package io.darkstar.net;


import org.springframework.util.Assert;

import java.util.Map;

public class DefaultConnectorFactory implements ConnectorFactory {

    @Override
    public Connector createListenConfig(String compactDefinition) {
        Assert.hasText(compactDefinition, "compactDefinition argument must contain text.");

        int colonIndex = compactDefinition.lastIndexOf(':');
        if (colonIndex > 0) {
            String address = compactDefinition.substring(0, colonIndex);
            String portString = compactDefinition.substring(colonIndex + 1);
            int port = PortParser.INSTANCE.parsePort(portString);
            return new DefaultConnector(address, port);
        }

        //otherwise, no colon char.  Let's see if what they specified was only a port or only an address:
        int port = -1;
        try {
            port = PortParser.INSTANCE.parsePort(compactDefinition);
            //it is a port, which means they didn't specify an address. Return the value:
            return new DefaultConnector(null, port);
        } catch (IllegalArgumentException ignored) {
            //not a port, just keep our default of -1
            return new DefaultConnector(compactDefinition, port);
        }
    }

    @Override
    public Connector createListenConfig(Map<String, Object> fullDefinition) {
        throw new UnsupportedOperationException("Not yet implemented!  Write me please!");
    }
}
