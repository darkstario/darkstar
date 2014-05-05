package io.darkstar.net;

public class DefaultPortParser implements PortParser {

    @Override
    public int parsePort(String portString) {
        int port;
        try {
            port = Integer.parseInt(portString);
        } catch (NumberFormatException e) {
            String msg = "Specified port '" + portString + "' must be an integer.";
            throw new IllegalArgumentException(msg, e);
        }

        if (port <= 0 || port > 65535) {
            String msg = "Specified port '" + portString + "' is not within the required range.  A port must be an " +
                    "integer greater than or equal to 1, but less than or equal to 65535";
            throw new IllegalArgumentException(msg);
        }

        return port;
    }
}
