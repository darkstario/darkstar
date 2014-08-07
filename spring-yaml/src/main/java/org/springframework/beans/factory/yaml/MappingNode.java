package org.springframework.beans.factory.yaml;

import java.util.Map;

public interface MappingNode extends CollectionNode<Map<String,Node>> {

    org.yaml.snakeyaml.nodes.MappingNode getSource();

}
