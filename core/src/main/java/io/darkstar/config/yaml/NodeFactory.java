package io.darkstar.config.yaml;

public interface NodeFactory {

    Node createGraph(String name, Object src);

    Node createGraph(final String name, final org.yaml.snakeyaml.nodes.Node value);
}
