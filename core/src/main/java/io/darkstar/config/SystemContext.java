package io.darkstar.config;

import io.darkstar.config.http.HttpContext;

public interface SystemContext extends Context {

    HttpContext getHttp();
}
