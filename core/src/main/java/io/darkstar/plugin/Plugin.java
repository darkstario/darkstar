package io.darkstar.plugin;

import io.darkstar.config.Context;

import java.util.Set;

public interface Plugin {

    String getName();

    Set<String> getSupportedAttributeNames();

    Object onConfigAttribute(String attributeName, Object configValue, Context applicableContext);
}
