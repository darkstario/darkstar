package org.springframework.beans.factory.yaml;

import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultMappingNode extends AbstractNode<Map<String,Node>> implements MappingNode {

    private final Map<String, Node> value;

    private final org.yaml.snakeyaml.nodes.MappingNode source;

    public DefaultMappingNode(Node parent, String name) {
        super(parent, name);
        this.value = new LinkedHashMap<>();
        this.source = null;
    }

    public DefaultMappingNode(Node parent, String name, org.yaml.snakeyaml.nodes.MappingNode source) {
        super(parent, name);
        this.value = new LinkedHashMap<>();
        this.source = source;
    }

    @Override
    public Map<String, Node> getValue() {
        return this.value;
    }

    @Override
    public org.yaml.snakeyaml.nodes.MappingNode getSource() {
        return this.source;
    }

    @Override
    public String getTag() {
        return getSource().getTag().getValue();
    }

    public void setKeyValuePair(String key, Node value) {
        this.value.put(key, value);
    }
}
