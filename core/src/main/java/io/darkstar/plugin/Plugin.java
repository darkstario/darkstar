package io.darkstar.plugin;

import io.darkstar.config.ContextAttribute;

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
}
