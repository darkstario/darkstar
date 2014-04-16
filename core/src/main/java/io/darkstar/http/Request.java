package io.darkstar.http;

import io.darkstar.net.Host;
import io.netty.handler.codec.http.HttpRequest;

import java.net.URI;

public interface Request<T> extends HttpRequest {

    URI getURI();

    long getStartTimestamp();

    String getScheme();

    boolean isSecure();

    Host getClientHost();

    Host getRequestedServerHost();

    Host getLocalHost();

    T copyRequest();
}
