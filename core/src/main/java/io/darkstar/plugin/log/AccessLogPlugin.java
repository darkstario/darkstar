package io.darkstar.plugin.log;

import com.stormpath.sdk.lang.Assert;
import io.darkstar.config.IdentifierName;
import io.darkstar.config.http.VirtualHost;
import io.darkstar.config.json.LogConfig;
import io.darkstar.plugin.AbstractPlugin;
import io.darkstar.plugin.stereotype.Plugin;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unchecked")
@Plugin
public class AccessLogPlugin extends AbstractPlugin {

    private static final Set<String> NAMES = IdentifierName.setOf("accessLog");

    @Override
    public Set<String> getDirectiveNames() {
        return NAMES;
    }

    @Override
    protected Object onVirtualHostDirective(String directiveName, Object directiveValue, VirtualHost vhost) {

        Map<String,Object> effective = new HashMap<>();

        Map<String,Object> parentProps = vhost.getParent().getAttribute("accessLog"); //might need to merge:
        if (!CollectionUtils.isEmpty(parentProps)) {
            effective.putAll(parentProps);
        }

        if (directiveValue instanceof String) { //single value support - default to the 'path' property:
            effective.put("path", directiveValue);
        } else {
            Assert.isInstanceOf(Map.class, directiveValue, "Unsupported access log directive value: " + directiveValue);
            Map<String,Object> props = (Map<String,Object>)directiveValue;
            effective.putAll(props);
        }

        LogConfig logConfig = new LogConfig();
        BeanWrapperImpl wrapper = new BeanWrapperImpl(logConfig);
        wrapper.setPropertyValues(effective);

        return logConfig;
    }
}
