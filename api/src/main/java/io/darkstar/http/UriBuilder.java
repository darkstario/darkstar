package io.darkstar.http;

import java.net.URI;

public interface UriBuilder {

    UriBuilder setAbsolute(boolean absolute);

    UriBuilder includeQuery(boolean includeQuery);

    URI build();
}
