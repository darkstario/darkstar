package io.darkstar.http;

public interface RequestEvent extends HttpEvent {

    Request getRequest();

    Pipeline getPipeline();
}
