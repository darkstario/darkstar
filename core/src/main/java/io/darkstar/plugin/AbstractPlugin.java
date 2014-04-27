package io.darkstar.plugin;

import io.darkstar.config.Context;
import io.darkstar.config.ContextAttribute;
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

    @SuppressWarnings("unchecked")
    @Override
    public Object onConfigAttribute(ContextAttribute attribute) {

        if (supports(attribute)) {

            Context context = attribute.getContext();

            if (context instanceof SystemContext) {
                return onSystemAttribute(attribute);
            } else if (context instanceof HttpContext) {
                return onHttpAttribute(attribute);
            } else if (context instanceof VirtualHost) {
                return onVirtualHostAttribute(attribute);
            } else if (context instanceof Route) {
                return onRouteAttribute(attribute);
            }
        }

        return attribute.getValue();
    }

    protected boolean supports(ContextAttribute attribute) {
        Directive directive = getDirectives().get(attribute.getName());
        return directive != null && directive.supports(attribute.getContext());
    }

    protected Object onSystemAttribute(ContextAttribute<SystemContext> attribute) {
        return attribute.getValue();
    }

    protected Object onHttpAttribute(ContextAttribute<HttpContext> attribute) {
        return attribute.getValue();
    }

    protected Object onVirtualHostAttribute(ContextAttribute<VirtualHost> attribute) {
        return attribute.getValue();
    }

    protected Object onRouteAttribute(ContextAttribute<Route> attribute) {
        return attribute.getValue();
    }
}
