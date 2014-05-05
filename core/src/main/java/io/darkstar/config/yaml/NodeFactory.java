package io.darkstar.config.yaml;

public interface NodeFactory {

    Node createGraph(String name, Object src);
}
