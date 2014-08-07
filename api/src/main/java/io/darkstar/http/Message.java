package io.darkstar.http;

public interface Message<T extends Headers> extends HttpObject {

    Version getProtocolVersion();

    T getHeaders();
}
