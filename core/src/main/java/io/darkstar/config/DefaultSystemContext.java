package io.darkstar.config;

import io.darkstar.config.http.HttpContext;

@SuppressWarnings("unchecked")
public class DefaultSystemContext extends DefaultContext implements SystemContext {

    public static final String NAME = "system";

    private HttpContext http;

    protected DefaultSystemContext() {
        super(NAME, null);
    }

    @Override
    public HttpContext getHttp() {
        return this.http;
    }
}
