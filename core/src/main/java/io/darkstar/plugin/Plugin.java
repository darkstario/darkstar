package io.darkstar.plugin;

import io.darkstar.config.ContextAttribute;
import io.darkstar.config.yaml.Node;

import java.util.Map;

public interface Plugin {

    String getName();

    /**
     * Name to Directive map.
     *
     * @return Name to Directive map.
     */
    Map<String, Directive> getDirectives();

    Object onConfigAttribute(ContextAttribute attribute);

    boolean supports(Node node);

    Object onConfigNode(Node node);
}
