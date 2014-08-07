package org.springframework.beans.factory.yaml;

public interface ScalarNode extends Node<Object> {

    org.yaml.snakeyaml.nodes.ScalarNode getSource();
}
