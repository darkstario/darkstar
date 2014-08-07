package io.darkstar.http;

public interface NettyHttpObjectSource<T extends io.netty.handler.codec.http.HttpObject> {

    T getHttpObject();
}
