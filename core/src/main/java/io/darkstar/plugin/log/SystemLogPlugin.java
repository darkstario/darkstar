package io.darkstar.plugin.log;

import io.darkstar.config.SystemContext;
import io.darkstar.plugin.AbstractPlugin;
import io.darkstar.plugin.Directive;
import io.darkstar.plugin.Directives;
import io.darkstar.plugin.stereotype.Plugin;

import java.util.Map;

@Plugin
public class SystemLogPlugin extends AbstractPlugin {

    public static final Map<String,Directive> DIRECTIVES = Directives.builder()
            .add("systemLog", SystemContext.class).buildMap();

    @Override
    public Map<String, Directive> getDirectives() {
        return DIRECTIVES;
    }

    //TODO: implement
}
