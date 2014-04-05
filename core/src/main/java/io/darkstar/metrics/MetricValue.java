package io.darkstar.metrics;

public class MetricValue<T> {

    private final String name;
    private final T value;
    private final long timeMillis;

    public MetricValue(String name, T value, long timeMillis) {
        this.name = name;
        this.value = value;
        this.timeMillis = timeMillis;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    public long getTimeMillis() {
        return timeMillis;
    }

    public boolean isAfter(MetricValue mv) {
        return this.timeMillis > mv.getTimeMillis();
    }

    public boolean isBefore(MetricValue mv) {
        return this.timeMillis < mv.getTimeMillis();
    }
}
