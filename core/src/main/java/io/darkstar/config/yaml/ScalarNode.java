package io.darkstar.config.yaml;

public interface ScalarNode extends Node<Object> {

    org.yaml.snakeyaml.nodes.ScalarNode getSource();
}
