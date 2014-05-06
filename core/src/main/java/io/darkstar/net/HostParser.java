package io.darkstar.net;

public interface HostParser {

    public static final HostParser INSTANCE = new DefaultHostParser();

    Host parse(String value);

}
