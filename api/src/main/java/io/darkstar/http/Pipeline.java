package io.darkstar.http;

public interface Pipeline {

    void proceed(Message message);

    void reply(Message message);

    void proceed(HttpEvent event);
}
