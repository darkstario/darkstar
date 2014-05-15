package io.darkstar.net;

public interface HostParser {

    public static final HostParser INSTANCE = new DefaultHostParser();

    Host parse(String value);

    Host parse(String value, int defaultPort);

}
