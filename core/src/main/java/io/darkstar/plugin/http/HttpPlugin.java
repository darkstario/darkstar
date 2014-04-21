package io.darkstar.plugin.http;

import io.darkstar.config.SystemContext;
import io.darkstar.config.http.DefaultHttpContext;
import io.darkstar.plugin.AbstractPlugin;
import io.darkstar.plugin.stereotype.Plugin;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Plugin
@SuppressWarnings("unchecked")
public class HttpPlugin extends AbstractPlugin {

    private static final String HTTP = "http";

    private static final Set<String> supportedAttributeNames = new HashSet<>(Arrays.asList(HTTP));

    @Override
    public Set<String> getSupportedAttributeNames() {
        return supportedAttributeNames;
    }

    @Override
    protected Object onSystemConfigAttribute(String attributeName, Object configValue, SystemContext ctx) {
        DefaultHttpContext httpContext = new DefaultHttpContext(ctx);

        //just relay the attributes that exist - other plugins will manipulate them as necessary:
        Map<String, Object> attributes = (Map<String, Object>) configValue;
        httpContext.putAttributes(attributes);

        return httpContext;
    }
}
