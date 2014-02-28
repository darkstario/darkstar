package com.stormpath.monban.event;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.eventbus.Subscribe;

import java.util.concurrent.Executors;
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

    public RequestListener() {
        executorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        requestMeter = new AtomicReference<>(metrics.meter(MetricRegistry.name(RequestListener.class, "requestMeter")));

        final Runnable oneSecondTask = new Runnable() {
            @Override
            public void run() {
                System.out.println(getStatsMessage());
                oneSecondCount.set(0);
            }
        };

        executorService.scheduleAtFixedRate(oneSecondTask, 0, 1l, TimeUnit.SECONDS);
    }

    public void shutdown() {
        this.executorService.shutdown();
    }

    private String getStatsMessage() {
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
    }

    @SuppressWarnings("UnusedDeclaration") //used by the event bus via reflection
    @Subscribe
    public void onRequestEvent(RequestEvent event) {
        inc();
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
