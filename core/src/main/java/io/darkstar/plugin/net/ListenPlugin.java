package io.darkstar.plugin.net;

import io.darkstar.config.ContextAttribute;
import io.darkstar.config.yaml.Node;
import io.darkstar.config.http.HttpContext;
import io.darkstar.config.http.VirtualHost;
import io.darkstar.net.DefaultListenConfigFactory;
import io.darkstar.net.Connector;
import io.darkstar.net.ListenConfigFactory;
import io.darkstar.net.ServerChannelManager;
import io.darkstar.plugin.AbstractPlugin;
import io.darkstar.plugin.Directive;
import io.darkstar.plugin.Directives;
import io.darkstar.plugin.stereotype.Plugin;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Plugin
public class ListenPlugin extends AbstractPlugin {

    private static final Map<String, Directive> DIRECTIVES = Directives.builder()
            .add("listen", Directives.setOf(HttpContext.class, VirtualHost.class)).buildMap();

    @Autowired
    private ServerChannelManager serverChannelManager;

    private final ListenConfigFactory listenConfigFactory;

    public ListenPlugin() {
        this.listenConfigFactory = new DefaultListenConfigFactory();
    }

    @Override
    public Map<String, Directive> getDirectives() {
        return DIRECTIVES;
    }

    @Override
    public boolean supports(Node node) {
        return  (node.getContext() instanceof HttpContext) && node.hasParent() &&
                node.getParent().getName().equals("http") || node.getParent().getName().equals("tls");
        //TODO: support listen directives inside of vhost entries?
    }

    @Override
    public Object onConfigNode(Node node) {

        Connector connector;

        boolean tls = node.getParent().getName().equals("tls");

        Object value = node.getValue();

        if (value instanceof String) {
            connector = listenConfigFactory.createListenConfig((String) value);
        } else if (value instanceof Map) {
            connector = listenConfigFactory.createListenConfig((Map<String, Object>) value);
        } else {
            throw new IllegalArgumentException("Unsupported listen value: " + value);
        }

        serverChannelManager.registerServerChannel(connector, tls);

        return connector;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Object onHttpAttribute(ContextAttribute<HttpContext> attribute) {

        Connector connector;

        Object value = attribute.getValue();
        if (value instanceof String) {
            connector = listenConfigFactory.createListenConfig((String) value);
        } else if (value instanceof Map) {
            connector = listenConfigFactory.createListenConfig((Map<String, Object>) value);
        } else {
            throw new IllegalArgumentException("Unsupported listen value: " + value);
        }

        return connector;
    }

    @Override
    protected Object onVirtualHostAttribute(ContextAttribute<VirtualHost> attribute) {
        Connector connector;

        Object value = attribute.getValue();
        if (value instanceof String) {
            connector = listenConfigFactory.createListenConfig((String) value);
        } else if (value instanceof Map) {
            connector = listenConfigFactory.createListenConfig((Map<String, Object>) value);
        } else {
            throw new IllegalArgumentException("Unsupported listen value: " + value);
        }

        return connector;
    }


}
