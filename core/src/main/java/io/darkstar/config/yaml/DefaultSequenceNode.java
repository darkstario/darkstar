package io.darkstar.config.yaml;

import com.stormpath.sdk.lang.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultSequenceNode extends AbstractNode<List<Node>> implements SequenceNode {

    private final List<Node> value;

    public DefaultSequenceNode(Node parent, String name) {
        super(parent, name);
        this.value = new ArrayList<>();
    }

    @Override
    public List<Node> getValue() {
        return Collections.unmodifiableList(value);
    }

    public void addNode(Node node) {
        Assert.notNull(node, "node argument cannot be null.");
        this.value.add(node);
    }
}
