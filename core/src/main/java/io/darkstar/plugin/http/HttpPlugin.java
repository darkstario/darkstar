package io.darkstar.plugin.http;

import io.darkstar.config.ContextAttribute;
import io.darkstar.config.SystemContext;
import io.darkstar.config.http.DefaultHttpContext;
import io.darkstar.plugin.AbstractPlugin;
import io.darkstar.plugin.Directive;
import io.darkstar.plugin.Directives;
import io.darkstar.plugin.stereotype.Plugin;

import java.util.Map;

@Plugin
@SuppressWarnings("unchecked")
public class HttpPlugin extends AbstractPlugin {

    public static final Map<String, Directive> DIRECTIVES = Directives.builder().add("http", SystemContext.class).buildMap();

    @Override
    public Map<String, Directive> getDirectives() {
        return DIRECTIVES;
    }

    @Override
    protected Object onSystemAttribute(ContextAttribute<SystemContext> attribute) {

        DefaultHttpContext httpContext = new DefaultHttpContext(attribute.getContext());

        //just relay the attributes that exist - other plugins will manipulate them as necessary:
        Map<String, Object> attributes = (Map<String, Object>) attribute.getValue();
        httpContext.putAttributes(attributes);

        return httpContext;
    }
}
