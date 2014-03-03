package com.stormpath.monban.metrics;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.stormpath.monban.event.RequestMetricsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;

//@Component
public class LoggingRequestMetricsReporter implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(LoggingRequestMetricsReporter.class);

    @Autowired
    private EventBus eventBus;

    @Override
    public void afterPropertiesSet() throws Exception {
        eventBus.register(this);
    }

    @SuppressWarnings("UnusedDeclaration") //called by the EventBus via reflection
    @Subscribe
    public void onEvent(RequestMetricsEvent e) {

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

        StringBuilder sb = new StringBuilder();
        Iterator<MetricValue> i = e.getMetricValues().iterator();
        MetricValue mv = i.next();
        sb.append(mv.getValue()).append(" requests | ");

        mv = i.next();
        sb.append(mv.getValue()).append(" req/s | ");

        mv = i.next();
        sb.append(mv.getValue()).append(" req/m | ");

        mv = i.next();
        sb.append(mv.getValue()).append(" req/5m | ");

        mv = i.next();
        sb.append(mv.getValue()).append(" req/15m");

        log.info(sb.toString());
    }
}
