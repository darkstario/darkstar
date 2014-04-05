package io.darkstar.ducksboard;

public interface DucksboardPayloadFactory {

    String value(Object value);

    String value(Object value, long secondsSinceEpoch);
}
