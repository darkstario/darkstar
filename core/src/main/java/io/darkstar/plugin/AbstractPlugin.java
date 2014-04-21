package io.darkstar.plugin;

import io.darkstar.config.Context;
import io.darkstar.config.SystemContext;
import io.darkstar.config.http.HttpContext;
import io.darkstar.config.http.Route;
import io.darkstar.config.http.VirtualHost;
import org.springframework.util.StringUtils;

public abstract class AbstractPlugin implements Plugin {

    @Override
    public String getName() {
        return StringUtils.uncapitalize(getClass().getSimpleName());
    }

    @Override
    public Object onConfigDirective(String directiveName, Object directiveValue, Context context) {
        if (!getDirectiveNames().contains(directiveName)) {
            return directiveValue;
        }

        if (context instanceof SystemContext) {
            return onSystemDirective(directiveName, directiveValue, (SystemContext) context);
        } else if (context instanceof HttpContext) {
            return onHttpDirective(directiveName, directiveValue, (HttpContext) context);
        } else if (context instanceof VirtualHost) {
            return onVirtualHostDirective(directiveName, directiveValue, (VirtualHost) context);
        } else if (context instanceof Route) {
            return onRouteDirective(directiveName, directiveValue, (Route) context);
        }

        return directiveValue;
    }

    protected Object onSystemDirective(String directiveName, Object directiveValue, SystemContext ctx) {
        return directiveValue;
    }

    protected Object onHttpDirective(String directiveName, Object directiveValue, HttpContext ctx) {
        return directiveValue;
    }

    protected Object onVirtualHostDirective(String directiveName, Object directiveValue, VirtualHost vhost) {
        return directiveValue;
    }

    protected Object onRouteDirective(String directiveName, Object directiveValue, Route route) {
        return directiveValue;
    }
}
