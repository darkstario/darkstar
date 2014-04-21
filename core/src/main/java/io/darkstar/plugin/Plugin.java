package io.darkstar.plugin;

import io.darkstar.config.Context;

import java.util.Set;

public interface Plugin {

    String getName();

    Set<String> getDirectiveNames();

    Object onConfigDirective(String directiveName, Object directiveValue, Context context);
}
