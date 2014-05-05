package io.darkstar.net;

public interface ServerChannelManager {

    void registerServerChannel(Connector connector, boolean tls);

    void init(); //TODO: rename

    void sync();
}
