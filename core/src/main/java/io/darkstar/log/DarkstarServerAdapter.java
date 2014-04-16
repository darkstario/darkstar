package io.darkstar.log;

import ch.qos.logback.access.spi.ServerAdapter;
import io.darkstar.http.Request;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import org.apache.shiro.util.CollectionUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DarkstarServerAdapter implements ServerAdapter {

    private final Request request;
    private final HttpResponse response;

    public DarkstarServerAdapter(Request request, HttpResponse response) {
        this.request = request;
        this.response = response;
    }

    @Override
    public long getRequestTimestamp() {
        return request.getStartTimestamp();
    }

    @Override
    public long getContentLength() {
        return HttpHeaders.getContentLength(response, -1);
    }

    @Override
    public int getStatusCode() {
        return response.getStatus().code();
    }

    @Override
    public Map<String, String> buildResponseHeaderMap() {
        Collection<String> headerNames = response.headers().names();

        Map<String, String> responseHeaderMap = new HashMap<String, String>();

        for (String headerName : headerNames) {
            Collection<String> values = response.headers().getAll(headerName);
            if (CollectionUtils.size(values) == 1) {
                responseHeaderMap.put(headerName, values.iterator().next());
            } else {
                StringBuilder sb = new StringBuilder();
                for (String value : values) {
                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append(value);
                }
                responseHeaderMap.put(headerName, sb.toString());
            }
        }

        return responseHeaderMap;
    }
}
