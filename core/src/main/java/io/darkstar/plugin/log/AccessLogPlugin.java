package io.darkstar.plugin.log;

import com.stormpath.sdk.lang.Assert;
import io.darkstar.config.ContextAttribute;
import io.darkstar.config.http.VirtualHost;
import io.darkstar.config.json.LogConfig;
import io.darkstar.plugin.AbstractPlugin;
import io.darkstar.plugin.Directive;
import io.darkstar.plugin.Directives;
import io.darkstar.plugin.stereotype.Plugin;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
@Plugin
public class AccessLogPlugin extends AbstractPlugin {

    public static final Map<String, Directive> DIRECTIVES = Directives.builder()
            .add("accessLog", VirtualHost.class).buildMap();

    @Override
    public Map<String, Directive> getDirectives() {
        return DIRECTIVES;
    }

    @Override
    protected Object onVirtualHostAttribute(ContextAttribute<VirtualHost> attribute) {

        Map<String, Object> effective = new HashMap<>();

        VirtualHost vhost = attribute.getContext();
        Object attributeValue = attribute.getValue();

        Map<String, Object> parentProps = vhost.getParent().getAttribute("accessLog"); //might need to merge:
        if (!CollectionUtils.isEmpty(parentProps)) {
            effective.putAll(parentProps);
        }

        if (attributeValue instanceof String) { //single value support - default to the 'path' property:
            effective.put("path", attributeValue);
        } else {
            Assert.isInstanceOf(Map.class, attributeValue, "Unsupported access log directive value: " + attributeValue);
            Map<String, Object> props = (Map<String, Object>) attributeValue;
            effective.putAll(props);
        }

        LogConfig logConfig = new LogConfig();
        BeanWrapperImpl wrapper = new BeanWrapperImpl(logConfig);
        wrapper.setPropertyValues(effective);

        return logConfig;
    }
}
