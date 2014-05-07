package io.darkstar.http;

import io.darkstar.net.DefaultHost;
import io.darkstar.net.Host;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.springframework.util.Assert;

import java.net.URI;
import java.net.URISyntaxException;

public class DefaultFullRequest extends DefaultFullHttpRequest implements FullRequest<FullRequest> {

    protected final boolean validateHeaders;
    private final URI uri;
    private final long startTimestamp;

    private String scheme;
    private boolean secure;
    private Host clientHost;
    private Host requestedServerHost;
    private Host localHost;

    public DefaultFullRequest(HttpVersion httpVersion, HttpMethod method, String uri) {
        this(httpVersion, method, uri, Unpooled.buffer(0));
    }

    public DefaultFullRequest(HttpVersion httpVersion, HttpMethod method, String uri, ByteBuf content) {
        this(httpVersion, method, uri, content, true);
    }

    public DefaultFullRequest(HttpVersion httpVersion, HttpMethod method, String uri, ByteBuf content, boolean validateHeaders) {
        this(httpVersion, method, uri, content, validateHeaders, null, System.currentTimeMillis());
    }

    protected DefaultFullRequest(HttpVersion httpVersion, HttpMethod method, String uri, ByteBuf content,
                                 boolean validateHeaders, URI _uri, long startTimestamp) {
        super(httpVersion, method, uri, content, validateHeaders);
        this.validateHeaders = validateHeaders;

        if (_uri == null) {
            try {
                this.uri = new URI(uri);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Cannot parse uri: " + uri);
            }
        } else {
            this.uri = _uri;
        }

        this.startTimestamp = startTimestamp;
    }

    @Override
    public URI getURI() {
        return this.uri;
    }

    @Override
    public long getStartTimestamp() {
        return this.startTimestamp;
    }

    @Override
    public String getScheme() {
        return this.scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
        if ("https".equalsIgnoreCase(scheme)) {
            this.secure = true;
        }
    }

    @Override
    public boolean isSecure() {
        return this.secure;
    }

    //not to be exposed - internal implementation detail:
    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    @Override
    public Host getClientHost() {
        //don't allow callers to manipulate the local value:
        return new DefaultHost(clientHost.getName(), clientHost.getPort());
    }

    public void setClientHost(Host host) {
        Assert.notNull(host, "host argument cannot be null.");
        this.clientHost = host;
    }

    @Override
    public Host getRequestedServerHost() {
        //don't allow callers to manipulate this value:
        return new DefaultHost(requestedServerHost.getName(), requestedServerHost.getPort());
    }

    public void setRequestedServerHost(Host host) {
        Assert.notNull(host, "host argument cannot be null.");
        this.requestedServerHost = host;
    }

    @Override
    public Host getLocalHost() {
        return this.localHost;
    }

    public void setLocalHost(Host host) {
        Assert.notNull(host, "host argument cannot be null.");
        this.localHost = host;
    }

    @Override
    public FullHttpRequest copy() {
        return copyRequest();
    }

    @Override
    public FullHttpRequest duplicate() {

        DefaultFullRequest duplicate = new DefaultFullRequest(getProtocolVersion(), getMethod(), getUri(),
                content().duplicate(), validateHeaders, getURI(), this.startTimestamp);

        duplicate.headers().set(headers());
        duplicate.trailingHeaders().set(trailingHeaders());

        duplicate.scheme = this.scheme;
        duplicate.secure = this.secure;
        duplicate.clientHost = this.clientHost;
        duplicate.requestedServerHost = this.requestedServerHost;
        duplicate.localHost = this.localHost;

        return duplicate;
    }

    @Override
    public FullRequest copyRequest() {
        URI uri;
        try {
            uri = new URI(this.uri.toString());
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Unable to represent already-encoded URI", e);
        }

        DefaultFullRequest copy = new DefaultFullRequest(getProtocolVersion(), getMethod(), getUri(),
                content().copy(), validateHeaders, uri, this.startTimestamp);

        copy.headers().set(headers());
        copy.trailingHeaders().set(trailingHeaders());

        copy.scheme = this.scheme;
        copy.secure = this.secure;
        copy.clientHost = this.clientHost;
        copy.requestedServerHost = this.requestedServerHost;
        copy.localHost = this.localHost;

        return copy;
    }
}
