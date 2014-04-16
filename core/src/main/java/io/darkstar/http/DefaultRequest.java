package io.darkstar.http;

import io.darkstar.net.Host;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.springframework.util.Assert;

import java.net.URI;
import java.net.URISyntaxException;

public class DefaultRequest extends DefaultHttpRequest implements Request<Request> {

    private final URI uri;
    private final long startTimestamp;
    protected final boolean validateHeaders;

    private String scheme;
    private boolean secure;
    private Host clientHost;
    private Host requestedServerHost;
    private Host localHost;

    public DefaultRequest(HttpVersion httpVersion, HttpMethod method, String uri) {
        this(httpVersion, method, uri, true);
    }

    public DefaultRequest(HttpVersion httpVersion, HttpMethod method, String uri, boolean validateHeaders) {
        this(httpVersion, method, uri, validateHeaders, null, System.currentTimeMillis());
    }

    protected DefaultRequest(HttpVersion version, HttpMethod method, String uri, boolean validateHeaders,
                             URI _uri, long startTimestamp) {
        super(version, method, uri, validateHeaders);
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
        //don't allow callers to manipulate this value:
        return new Host() {
            @Override
            public String getName() {
                return clientHost.getName();
            }

            @Override
            public int getPort() {
                return clientHost.getPort();
            }
        };
    }

    public void setClientHost(Host host) {
        Assert.notNull(host, "host argument cannot be null.");
        this.clientHost = host;
    }

    @Override
    public Host getRequestedServerHost() {
        //don't allow callers to manipulate this value:
        return new Host() {
            @Override
            public String getName() {
                return requestedServerHost.getName();
            }

            @Override
            public int getPort() {
                return requestedServerHost.getPort();
            }
        };
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
    public Request copyRequest() {

        URI uri;
        try {
            uri = new URI(this.uri.toString());
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Unable to represent already-encoded URI", e);
        }

        DefaultRequest copy = new DefaultRequest(getProtocolVersion(), getMethod(), getUri(),
                validateHeaders, uri, this.startTimestamp);

        copy.headers().set(headers());

        copy.scheme = this.scheme;
        copy.secure = this.secure;
        copy.clientHost = this.clientHost;
        copy.requestedServerHost = this.requestedServerHost;
        copy.localHost = this.localHost;

        return copy;
    }
}
