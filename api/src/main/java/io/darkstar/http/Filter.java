package io.darkstar.http;

public interface Filter {

    //Option 1:
    void filter(RequestEvent e);

    void filter(ResponseEvent e);


    //Option 2:

    void filter(Request request, Pipeline pipeline);

    void filterEntity(EntityContent chunk, Request request, Pipeline pipeline);


    void filter(Response response, Pipeline pipeline);

    void filterEntity(EntityContent chunk, Response response, Pipeline pipeline);

}
