package com.stormpath.monban.event;

import com.stormpath.monban.metrics.MetricValue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RequestMetricsEvent extends Event {

    private final List<MetricValue> metricValues;

    public RequestMetricsEvent(List<MetricValue> metricValues) {
        this.metricValues = Collections.unmodifiableList(metricValues);
    }

    public RequestMetricsEvent(MetricValue... metricValues) {
        this.metricValues = Collections.unmodifiableList(Arrays.asList(metricValues));
    }

    public List<MetricValue> getMetricValues() {
        return metricValues;
    }
}
