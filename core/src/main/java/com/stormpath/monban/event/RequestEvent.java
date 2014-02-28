package com.stormpath.monban.event;

public class RequestEvent extends Event {

    private final long requestId;
    private final int numBytes;

    public RequestEvent(long requestId, int numBytes) {
        this.requestId = requestId;
        this.numBytes = numBytes;
    }

}
