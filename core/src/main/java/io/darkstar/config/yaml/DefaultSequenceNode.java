package io.darkstar.config.yaml;

import com.stormpath.sdk.lang.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultSequenceNode extends AbstractNode<List<Node>> implements SequenceNode {

    private final List<Node> value;
    private final org.yaml.snakeyaml.nodes.SequenceNode source;

    public DefaultSequenceNode(Node parent, String name) {
        super(parent, name);
        this.value = new ArrayList<>();
        this.source = null;
    }

    public DefaultSequenceNode(Node parent, String name, org.yaml.snakeyaml.nodes.SequenceNode source) {
        super(parent, name);
        this.value = new ArrayList<>();
        this.source = source;
    }

    @Override
    public List<Node> getValue() {
        return Collections.unmodifiableList(value);
    }

    @Override
    public org.yaml.snakeyaml.nodes.SequenceNode getSource() {
        return this.source;
    }

    @Override
    public String getTag() {
        return getSource().getTag().getValue();
    }

    public void addNode(Node node) {
        Assert.notNull(node, "node argument cannot be null.");
        this.value.add(node);
    }
}
