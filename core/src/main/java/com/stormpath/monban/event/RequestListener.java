package com.stormpath.monban.event;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.Subscribe;
import com.stormpath.monban.datadog.DatadogPoster;
import com.stormpath.monban.ducksboard.DucksboardPoster;
import org.apache.http.client.HttpClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class RequestListener {

    private final AtomicLong requestCount = new AtomicLong(0);
    private final AtomicLong oneSecondCount = new AtomicLong(0);
    private final AtomicReference<Meter> requestMeter;

    @SuppressWarnings("FieldCanBeLocal")
    private final MetricRegistry metrics = new MetricRegistry();
    private final ScheduledExecutorService executorService;
    private final DucksboardPoster ducksboardPoster;
    private final DatadogPoster datadogPoster;

    public RequestListener(ScheduledExecutorService executorService, HttpClient httpClient, String ducksboardApiKey, String datadogApiKey) {
        this.executorService = executorService;
        this.requestMeter = new AtomicReference<>(metrics.meter(MetricRegistry.name(RequestListener.class, "requestMeter")));
        this.ducksboardPoster = new DucksboardPoster(httpClient, ducksboardApiKey);
        this.datadogPoster = new DatadogPoster(httpClient, datadogApiKey);

        //String testJson = toDucksboardJson(System.currentTimeMillis() / 1000, 432);
        //ducksboardPoster.post("fe-reqs", testJson);
    }

    public void run() {
        final Runnable oneSecondTask = new Runnable() {
            @Override
            public void run() {
                //System.out.println("Running RequestListener oneSecondTask");

                final long millis = System.currentTimeMillis();
                final long seconds = millis / 1000;

                final long val = requestCount.get();

                // ============== DUCKSBOARD ===============
                /*String json = toDucksboardJson(seconds, val);
                System.out.println("Ducksboard JSON (fe-reqs): " + json);
                ducksboardPoster.post("fe-reqs", json);

                val = oneSecondCount.get();
                json = toDucksboardJson(seconds, val);
                ducksboardPoster.post("reqPerSec", json);

                String sval = format(requestMeter.get().getOneMinuteRate());
                json = toDucksboardJson(seconds, sval);
                ducksboardPoster.post("reqPerMin", json);

                sval = format(requestMeter.get().getFiveMinuteRate());
                json = toDucksboardJson(seconds, sval);
                ducksboardPoster.post("reqPer5Min", json);

                sval = format(requestMeter.get().getFifteenMinuteRate());
                json = toDucksboardJson(seconds, sval);
                ducksboardPoster.post("reqPer15Min", json); */


                // ================= DATADOG ==================
                final Runnable requestor = new Runnable() {
                    @Override
                    public void run() {
                        String json = toDatadogJson("requests.frontend", millis, val);
                        datadogPoster.post(json);
                        //To change body of implemented methods use File | Settings | File Templates.
                    }
                };
                executorService.submit(requestor);

                oneSecondCount.set(0);
            }
        };

        executorService.scheduleAtFixedRate(oneSecondTask, 0, 1l, TimeUnit.SECONDS);
    }

    /*private String getStatsMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(requestCount.get());
        sb.append(" requests (");
        sb.append(oneSecondCount.get());
        sb.append(" req/s | ");
        sb.append(format(requestMeter.get().getOneMinuteRate()));
        sb.append(" req/m | ");
        sb.append(format(requestMeter.get().getFiveMinuteRate()));
        sb.append(" req/5m | ");
        sb.append(format(requestMeter.get().getFifteenMinuteRate()));
        sb.append(" req/15m)");
        return sb.toString();
    }*/

    @SuppressWarnings("UnusedDeclaration") //used by the event bus via reflection
    @Subscribe
    public void onRequestEvent(RequestEvent event) {
        inc();
    }

    private String toDucksboardJson(long seconds, Object value) {
        StringBuilder sb = new StringBuilder("{\"timestamp\":").append(seconds).append(",\"value\":");
        if (value instanceof CharSequence) {
            sb.append("\"").append(value).append(("\""));
        } else {
            sb.append(value);
        }
        sb.append("}");
        return sb.toString();
    }

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

    public String format(double value) {
        if (value < 0.01) {
            return "0.0";
        }
        return String.format("%.2f", value);
    }

    private void inc() {
        requestCount.incrementAndGet();
        oneSecondCount.incrementAndGet();
        requestMeter.get().mark();
    }

}
