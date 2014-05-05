package io.darkstar.config.yaml;

import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultMappingNode extends AbstractNode<Map<String,Node>> implements MappingNode {

    private final Map<String, Node> value;

    protected DefaultMappingNode(Node parent, String name) {
        super(parent, name);
        this.value = new LinkedHashMap<>();
    }

    @Override
    public Map<String, Node> getValue() {
        return this.value;
    }

    public void setKeyValuePair(String key, Node value) {
        this.value.put(key, value);
    }
}
