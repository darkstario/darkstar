package io.darkstar.config.yaml;

import com.stormpath.sdk.lang.Assert;
import org.yaml.snakeyaml.nodes.*;

import java.util.List;
import java.util.Map;

public class DefaultNodeFactory implements NodeFactory {

    @Override
    public Node createGraph(String name, Object value) {
        return recurse(name, value, null);
    }

    @Override
    public Node createGraph(String name, org.yaml.snakeyaml.nodes.Node value) {
        return recurseYaml(name, value, null);
    }

    @SuppressWarnings("unchecked")
    private Node recurseYaml(final String name, final org.yaml.snakeyaml.nodes.Node value, final Node parent) {

        if (value instanceof org.yaml.snakeyaml.nodes.MappingNode) {

            org.yaml.snakeyaml.nodes.MappingNode mappingNode = (org.yaml.snakeyaml.nodes.MappingNode)value;

            List<NodeTuple> nodeTuples = mappingNode.getValue();

            DefaultMappingNode node = new DefaultMappingNode(parent, name, mappingNode);

            for(NodeTuple tuple : nodeTuples) {

                Assert.isInstanceOf(org.yaml.snakeyaml.nodes.ScalarNode.class, tuple.getKeyNode());
                String entryKeyString = ((org.yaml.snakeyaml.nodes.ScalarNode)tuple.getKeyNode()).getValue();

                org.yaml.snakeyaml.nodes.Node valueNode = tuple.getValueNode();

                Node converted = recurseYaml(entryKeyString, valueNode, node);

                node.setKeyValuePair(entryKeyString, converted);
            }

            return node;
        }

        if (value instanceof org.yaml.snakeyaml.nodes.SequenceNode) {

            org.yaml.snakeyaml.nodes.SequenceNode sequenceNode = (org.yaml.snakeyaml.nodes.SequenceNode)value;

            List<org.yaml.snakeyaml.nodes.Node> nodes = sequenceNode.getValue();

            DefaultSequenceNode node = new DefaultSequenceNode(parent, name, sequenceNode);

            for(org.yaml.snakeyaml.nodes.Node aNode : nodes) {
                Node converted = recurseYaml(null, aNode, node);
                node.addNode(converted);
            }

            return node;
        }

        Assert.isInstanceOf(org.yaml.snakeyaml.nodes.ScalarNode.class, value);
        org.yaml.snakeyaml.nodes.ScalarNode scalarNode = (org.yaml.snakeyaml.nodes.ScalarNode)value;

        return new DefaultScalarNode(parent, name, scalarNode);
    }

    @SuppressWarnings("unchecked")
    private Node recurse(final String name, final Object value, final Node parent) {

        if (value instanceof Map) {

            Map<Object, Object> m = (Map<Object, Object>) value;

            DefaultMappingNode node = new DefaultMappingNode(parent, name);

            for (Map.Entry<Object, Object> entry : m.entrySet()) {
                String entryKeyString = String.valueOf(entry.getKey());
                Object entryValue = entry.getValue();

                Node converted = recurse(entryKeyString, entryValue, node);

                node.setKeyValuePair(entryKeyString, converted);
            }

            return node;
        }

        if (value instanceof List) {

            List list = (List) value;

            DefaultSequenceNode node = new DefaultSequenceNode(parent, name);

            for (Object o : list) {
                Node converted = recurse(null, o, node);
                node.addNode(converted);
            }

            return node;
        }

        return new DefaultScalarNode(parent, name, value);
    }
}
