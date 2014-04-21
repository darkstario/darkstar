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
    public Object onConfigAttribute(String attributeName, Object configValue, Context applicableContext) {
        if (!getSupportedAttributeNames().contains(attributeName)) {
            return configValue;
        }

        if (applicableContext instanceof SystemContext) {
            return onSystemConfigAttribute(attributeName, configValue, (SystemContext)applicableContext);
        } else if (applicableContext instanceof HttpContext) {
            return onHttpConfigAttribute(attributeName, configValue, (HttpContext)applicableContext);
        } else if (applicableContext instanceof VirtualHost) {
            return onVirtualHostConfigAttribute(attributeName, configValue, (VirtualHost) applicableContext);
        } else if (applicableContext instanceof Route) {
            return onRouteConfigAttribute(attributeName, configValue, (Route)applicableContext);
        }

        return configValue;
    }

    protected Object onSystemConfigAttribute(String attributeName, Object configValue, SystemContext ctx) {
        return configValue;
    }

    protected Object onHttpConfigAttribute(String attributeName, Object configValue, HttpContext ctx) {
        return configValue;
    }

    protected Object onVirtualHostConfigAttribute(String attributeName, Object configValue, VirtualHost applicableContext) {
        return configValue;
    }

    protected Object onRouteConfigAttribute(String attributeName, Object configValue, Route applicableContext) {
        return configValue;
    }
}
