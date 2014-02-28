package com.stormpath.monban.event;

public class BytesEvent extends Event {

    private final long count;
    private final boolean inbound;

    public BytesEvent(long count, boolean inbound) {
        this.count = count;
        this.inbound = inbound;
    }

    public long getCount() {
        return count;
    }

    public boolean isInbound() {
        return inbound;
    }
}
