package io.darkstar.plugin;

import io.darkstar.config.Context;

public interface Directive {

    String getName();

    boolean supports(Context context);
}
