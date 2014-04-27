package io.darkstar.plugin.tls;

import io.darkstar.config.ContextAttribute;
import io.darkstar.config.http.VirtualHost;
import io.darkstar.config.json.TlsConfig;
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
public class TlsPlugin extends AbstractPlugin {

    public static final Map<String, Directive> DIRECTIVES = Directives.builder().add("tls", VirtualHost.class).buildMap();

    @Override
    public Map<String, Directive> getDirectives() {
        return DIRECTIVES;
    }

    @Override
    protected Object onVirtualHostAttribute(ContextAttribute<VirtualHost> attribute) {

        Object value = attribute.getValue();
        VirtualHost vhost = attribute.getContext();

        if (!(value instanceof Map)) {
            throw new IllegalArgumentException("Unsupported tls directive value: " + value);
        }

        Map<String, Object> props = (Map<String, Object>) value;
        Map<String, Object> effective = new HashMap<>();

        Map<String, Object> parentProps = vhost.getParent().getAttribute("tls"); //might need to merge:
        if (!CollectionUtils.isEmpty(parentProps)) {
            effective.putAll(parentProps);
        }
        effective.putAll(props);

        TlsConfig tlsConfig = new TlsConfig();
        BeanWrapperImpl wrapper = new BeanWrapperImpl(tlsConfig);
        wrapper.setPropertyValues(effective);

        return tlsConfig;
    }
}
