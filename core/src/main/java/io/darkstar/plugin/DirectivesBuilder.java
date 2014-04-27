package io.darkstar.plugin;

import io.darkstar.config.Context;

import java.util.Map;
import java.util.Set;

public interface DirectivesBuilder {

    DirectivesBuilder add(String name, Class<? extends Context> contextClass);

    DirectivesBuilder add(String name, Set<Class<? extends Context>> contextClasses);

    Map<String,Directive> buildMap();
}
