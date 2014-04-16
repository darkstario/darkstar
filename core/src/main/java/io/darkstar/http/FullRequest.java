package io.darkstar.http;

import io.netty.handler.codec.http.FullHttpRequest;

public interface FullRequest<T> extends FullHttpRequest, Request<T> {
}
