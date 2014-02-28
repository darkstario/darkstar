package com.stormpath.monban.event;

public class RequestEvent {

    private final long millis;

    private final long requestId;
    private final int numBytes;

    public RequestEvent(long requestId, int numBytes) {
        millis = System.currentTimeMillis();
        this.requestId = requestId;
        this.numBytes = numBytes;
    }

    public long getMillis() {
        return millis;
    }
}
