package io.darkstar.plugin.tls;

import io.darkstar.config.IdentifierName;
import io.darkstar.config.http.VirtualHost;
import io.darkstar.config.json.TlsConfig;
import io.darkstar.plugin.AbstractPlugin;
import io.darkstar.plugin.stereotype.Plugin;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unchecked")
@Plugin
public class TlsPlugin extends AbstractPlugin {

    private static final Set<String> NAMES = IdentifierName.setOf("tls");

    @Override
    public Set<String> getDirectiveNames() {
        return NAMES;
    }

    @Override
    protected Object onVirtualHostDirective(String directiveName, Object directiveValue, VirtualHost vhost) {
        if (!(directiveValue instanceof Map)) {
            throw new IllegalArgumentException("Unsupported tls directive value: " + directiveValue);
        }

        Map<String,Object> props = (Map<String,Object>)directiveValue;
        Map<String,Object> effective = new HashMap<>();

        Map<String,Object> parentProps = vhost.getParent().getAttribute("tls"); //might need to merge:
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
