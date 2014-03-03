package com.stormpath.monban.ducksboard;

public interface DucksboardPayloadFactory {

    String value(Object value);

    String value(Object value, long secondsSinceEpoch);
}
