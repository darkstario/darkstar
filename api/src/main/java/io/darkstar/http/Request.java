package io.darkstar.http;

import io.darkstar.net.Host;

import java.net.URI;

public interface Request extends Message<Headers> {

    Method getMethod();

    URI getUri();

    UriBuilder getUriBuilder();

    String getUriRaw();

    /**
     * Returns the host specified in the request, obtained from either the request URI or HOST header according to
     * HTTP <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec5.html#sec5.1.2">parsing/precedence rules</a>.
     *
     * @return
     */
    Host getHost();
}
