package io.darkstar.config.yaml;

import java.util.List;
import java.util.Map;

public class DefaultNodeFactory implements NodeFactory {

    @Override
    public Node createGraph(String name, Object value) {
        return recurse(name, value, null);
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
