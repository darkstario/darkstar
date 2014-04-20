package io.darkstar.config;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

@SuppressWarnings("unchecked")
public class DefaultNode implements Node {

    private Node parent;
    private final String name;
    private final Object value;

    private ExpressionParser parser;
    private EvaluationContext evaluationContext;

    public DefaultNode(String name, Object value) {
        this(name, value, null);
    }

    public DefaultNode(String name, Object value, Node parent) {
        Assert.hasText(name, "name argument must be a populated String value.");
        Assert.notNull(value, "value argument cannot be null.");
        this.name = name;
        this.value = value;
        this.parent = parent;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean hasParent() {
        return parent != null;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    @Override
    public Node getParent() {
        return parent;
    }

    @Override
    public <T> T getValue() {
        return (T) value;
    }

    @Override
    public <T> T getValue(String expr) {

        Assert.hasText(expr, "expr argument must be a populated String value.");

        Object value = this.value;

        if (!(value instanceof Map) || !(value instanceof List)) {
            throw new IllegalArgumentException("Expressions are only supported for map and list values.");
        }

        Scanner scanner = new Scanner(expr).useDelimiter("\\.");

        while (scanner.hasNext()) {
            String propName = scanner.next();

            int index = -1;

            int lbIndex = propName.indexOf('[');
            int rbIndex = propName.lastIndexOf(']');

            if (lbIndex > -1) {
                String indexString = propName.substring(lbIndex + 1, rbIndex);
                index = Integer.parseInt(indexString);
            }

            if (index != -1) {
                List list = (List) value;
                value = list.get(index);
            }
        }

        return (T) value;
    }
}
