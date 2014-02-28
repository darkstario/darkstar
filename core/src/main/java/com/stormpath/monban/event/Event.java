package com.stormpath.monban.event;

public abstract class Event {

    private final long millis;

    public Event() {
        this.millis = System.currentTimeMillis();
    }

    public long getMillis() {
        return millis;
    }
}
