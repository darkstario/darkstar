package io.darkstar.config;

import org.springframework.context.ApplicationContext;

public interface ConfigHandler {

    boolean supports(String name, Object value);

    void handle(Node node, ApplicationContext current);
}
