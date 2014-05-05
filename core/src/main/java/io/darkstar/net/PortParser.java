package io.darkstar.net;

public interface PortParser {

    public static final PortParser INSTANCE = new DefaultPortParser();

    int parsePort(String portString);

}
