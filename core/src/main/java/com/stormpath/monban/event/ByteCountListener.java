package com.stormpath.monban.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class ByteCountListener {

    private final AtomicLong inboundCount = new AtomicLong(0);
    private final AtomicLong outboundCount = new AtomicLong(0);

    public ByteCountListener() {
    }

    @SuppressWarnings("UnusedDeclaration") //used by the event bus via reflection
    @Subscribe
    public void onByteEvent(BytesEvent event) {
        long numBytes = event.getCount();

        if (numBytes > 0) {
            AtomicLong count = (event.isInbound() ? inboundCount : outboundCount);
            while (true) {
                long current = count.get();
                long incremented = current + numBytes;
                if (count.compareAndSet(current, incremented)) {
                    break;
                }
            }
        }
    }

    public void run() {
        final Runnable oneSecondTask = new Runnable() {
            public void run() {
                final long millis = System.currentTimeMillis();
                final long inbound = inboundCount.get();
                final long outbound = outboundCount.get();

                //String json = toDucksboardJson(seconds, inbound);
                //System.out.println("Ducksboard JSON (net-in): " + json);
                //ducksboardPoster.post("net-in", json);


                /*
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        String json = toDatadogJson("network.in", millis, inbound);
                        datadogPoster.post(json);
                    }
                });

                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        String json = toDatadogJson("network.out", millis, outbound);
                        datadogPoster.post(json);
                    }
                });
                */

                //json = toDucksboardJson(seconds, outbound);
                //ducksboardPoster.post("net-out", json);
                //System.out.println("Ducksboard JSON (net-out): " + json);
            }
        };
        //this.executorService.scheduleAtFixedRate(oneSecondTask, 0, 1, TimeUnit.SECONDS);
    }

    /*
    @SuppressWarnings("unchecked")
    private String toDatadogJson(String name, long millis, Object value) {

        Map item = new LinkedHashMap();
        item.put("metric", "monban." + name + ".metric");

        List points = new ArrayList();
        points.add(millis);
        points.add(value);

        item.put("points", Arrays.asList(points));
        item.put("type", "gauge");
        item.put("host", "monban.stormpath.com");
        item.put("tags", Arrays.asList("environment:monban"));

        Map body = new LinkedHashMap<>();
        body.put("series", Arrays.asList(item));


        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String format(double value) {
        if (value < 0.01) {
            return "0.0";
        }
        return String.format("%.2f", value);
    }
    */
}
