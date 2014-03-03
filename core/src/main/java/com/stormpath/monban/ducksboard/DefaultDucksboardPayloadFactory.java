package com.stormpath.monban.ducksboard;


import org.springframework.stereotype.Component;

@Component
public class DefaultDucksboardPayloadFactory implements DucksboardPayloadFactory {

    @Override
    public String value(Object value) {
        StringBuilder sb = new StringBuilder("{\"value\":");
        if (value instanceof CharSequence) {
            sb.append("\"").append(value).append(("\""));
        } else {
            sb.append(value);
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String value(Object value, long secondsSinceEpoch) {
        StringBuilder sb = new StringBuilder("{\"timestamp\":").append(secondsSinceEpoch).append(",\"value\":");
        if (value instanceof CharSequence) {
            sb.append("\"").append(value).append(("\""));
        } else {
            sb.append(value);
        }
        sb.append("}");
        return sb.toString();
    }
}
