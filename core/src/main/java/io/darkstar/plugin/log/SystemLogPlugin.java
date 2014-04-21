package io.darkstar.plugin.log;

import io.darkstar.config.IdentifierName;
import io.darkstar.plugin.AbstractPlugin;
import io.darkstar.plugin.stereotype.Plugin;

import java.util.Set;

@Plugin
public class SystemLogPlugin extends AbstractPlugin {

    private final Set<String> NAMES = IdentifierName.setOf("systemLog");

    @Override
    public Set<String> getDirectiveNames() {
        return NAMES;
    }

    //TODO: implement
}
