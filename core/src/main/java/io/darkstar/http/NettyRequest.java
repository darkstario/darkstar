package io.darkstar.http;

import io.darkstar.net.Host;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.HttpObject;
import org.springframework.util.Assert;

import java.net.URI;

public class NettyRequest implements Request, NettyHttpObjectSource<HttpRequest> {

    private final HttpRequest NETTY_REQUEST;
    private final Method      METHOD;
    private final URI         URI;
    private final Version     PROTOCOL_VERSION;
    private final Headers     HEADERS;
    private final Host        HOST;

    public NettyRequest(HttpRequest nettyRequest, URI uri, Host host) {

        Assert.notNull(nettyRequest, "nettyRequest argument cannot be null.");
        Assert.notNull(uri, "uri argument cannot be null.");
        Assert.notNull(host, "host argument cannot be null.");

        NETTY_REQUEST = nettyRequest;
        URI = uri;
        HOST = host;

        METHOD = Method.valueOf(NETTY_REQUEST.getMethod().name());

        PROTOCOL_VERSION = Version.valueOf(NETTY_REQUEST.getProtocolVersion().text());

        HttpHeaders nettyHeaders = nettyRequest.headers();
        if (nettyHeaders == null) {
            nettyHeaders = HttpHeaders.EMPTY_HEADERS;
        }
        HEADERS = new NettyHeaders(nettyHeaders);
    }

    @Override
    public Method getMethod() {
        return METHOD;
    }

    @Override
    public URI getUri() {
        return URI;
    }

    @Override
    public UriBuilder getUriBuilder() {
        throw new UnsupportedOperationException("Not Yet Implemented.");
    }

    @Override
    public String getUriRaw() {
        return NETTY_REQUEST.getUri();
    }

    @Override
    public Version getProtocolVersion() {
        return PROTOCOL_VERSION;
    }

    @Override
    public Headers getHeaders() {
        return HEADERS;
    }

    @Override
    public Host getHost() {
        return HOST;
    }

    @Override
    public HttpRequest getHttpObject() {
        return NETTY_REQUEST;
    }
}
