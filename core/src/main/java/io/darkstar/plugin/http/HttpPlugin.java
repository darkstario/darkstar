package io.darkstar.plugin.http;

import io.darkstar.config.IdentifierName;
import io.darkstar.config.SystemContext;
import io.darkstar.config.http.DefaultHttpContext;
import io.darkstar.plugin.AbstractPlugin;
import io.darkstar.plugin.stereotype.Plugin;

import java.util.Map;
import java.util.Set;

@Plugin
@SuppressWarnings("unchecked")
public class HttpPlugin extends AbstractPlugin {

    private static final String HTTP = "http";

    private static final Set<String> NAMES = IdentifierName.setOf(HTTP);

    @Override
    public Set<String> getDirectiveNames() {
        return NAMES;
    }

    @Override
    protected Object onSystemDirective(String directiveName, Object directiveValue, SystemContext ctx) {
        DefaultHttpContext httpContext = new DefaultHttpContext(ctx);

        //just relay the attributes that exist - other plugins will manipulate them as necessary:
        Map<String, Object> attributes = (Map<String, Object>) directiveValue;
        httpContext.putAttributes(attributes);

        return httpContext;
    }
}
