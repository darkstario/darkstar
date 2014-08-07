package org.springframework.beans.factory.yaml;

import java.util.List;

public interface SequenceNode extends CollectionNode<List<Node>> {

    org.yaml.snakeyaml.nodes.SequenceNode getSource();

}
