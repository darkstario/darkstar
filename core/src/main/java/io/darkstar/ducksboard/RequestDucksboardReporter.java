package io.darkstar.ducksboard;

//@Component
public class RequestDucksboardReporter { /* implements InitializingBean {

    @Autowired
    private AsyncListenableTaskExecutor executor;

    @Autowired
    private EventBus eventBus;

    @Autowired
    private DucksboardPayloadFactory factory;

    @Autowired
    private DucksboardPoster ducksboardPoster;

    private final Map<String, MetricValue> lastReportedValues = new ConcurrentHashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        eventBus.register(this);
    }

    @SuppressWarnings("UnusedDeclaration") //called by the EventBus via reflection
    @Subscribe
    public void onEvent(RequestMetricsEvent e) {
        for (MetricValue<?> mv : e.getMetricValues()) {
            executor.submit(new MetricValueReporter(ducksboardPoster, factory, mv));
        }
    }

    private final class MetricValueReporter implements Runnable {

        private final DucksboardPoster poster;
        private final DucksboardPayloadFactory factory;
        private final MetricValue<?> metricValue;

        public MetricValueReporter(DucksboardPoster poster, DucksboardPayloadFactory factory, MetricValue metricValue) {
            this.poster = poster;
            this.factory = factory;
            this.metricValue = metricValue;
        }

        @Override
        public void run() {
            final String ducksboardLabel = metricValue.getName();
            final Object valueToReport = sanitize(metricValue.getValue());

            //only report if the new value is different than the last reported value.  This prevents us
            //from flooding Ducksboard if there is no data change:

            MetricValue last = lastReportedValues.get(ducksboardLabel);

            if (last == null || (metricValue.isAfter(last) && !valueToReport.equals(last.getValue()))) {

                //Ducksboard requires time in secs, not millis:
                final long secondsSinceEpoch = metricValue.getTimeMillis() / 1000;
                final String json = factory.value(valueToReport, secondsSinceEpoch);

                poster.post(ducksboardLabel, json);

                //we keep the previous record of what we reported to Ducksboard, not the raw metric value
                //(which has a precision greater than what we report).  We only want to post if what we report
                //is different from what we last reported:
                MetricValue<Object> reported = new MetricValue<>(ducksboardLabel, valueToReport, metricValue.getTimeMillis());
                lastReportedValues.put(ducksboardLabel, reported);
            }

            //else, nothing has changed since this metric's last update, so don't post anything
        }

        private Object sanitize(Object value) {
            //we don't want to show huge numbers: hundredths is good enough granularity for reporting:
            if (value instanceof Double) {
                if (((double) value) < 0.01) {
                    value = 0.00;
                }
            } else if (value instanceof Float) {
                if (((float) value) < 0.01f) {
                    value = 0.00;
                }
            }
            return value;
        }

        /*
        public static String format(double value) {
            if (value < 0.01) {
                return "0.0";
            }
            return String.format("%.2f", value);
        }

    }

    */
}
