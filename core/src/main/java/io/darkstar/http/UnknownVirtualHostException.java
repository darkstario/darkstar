package io.darkstar.http;

public class UnknownVirtualHostException extends RuntimeException {

    public UnknownVirtualHostException(String msg) {
        super(msg);
    }
}
