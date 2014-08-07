package io.darkstar.event;

//@Component
public class RequestMetricsService { /* implements InitializingBean {

    @Autowired
    private TaskScheduler executor;

    @Autowired
    private MetricRegistry metricRegistry;

    @Autowired
    private EventBus eventBus;

    private final AtomicLong requestCount = new AtomicLong(0);
    private final AtomicLong oneSecondCount = new AtomicLong(0);
    private Meter requestMeter;

    private volatile boolean sendEvents = false;

    public RequestMetricsService() {
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.requestMeter = metricRegistry.meter(MetricRegistry.name(getClass(), "requestMeter"));
        run();
        this.eventBus.register(this);
    }

    public void run() {

        final Runnable oneSecondTask = new Runnable() {
            @Override
            public void run() {
                if (!sendEvents) {
                    return;
                }
                long millis = System.currentTimeMillis();

                RequestMetricsEvent event = new RequestMetricsEvent(
                        new MetricValue<>("reqCount", requestCount.get(), millis),
                        new MetricValue<>("reqPerSec", oneSecondCount.get(), millis),
                        new MetricValue<>("reqPerMin", requestMeter.getOneMinuteRate(), millis),
                        new MetricValue<>("reqPer5Min", requestMeter.getFiveMinuteRate(), millis),
                        new MetricValue<>("reqPer15Min", requestMeter.getFifteenMinuteRate(), millis)
                );

                oneSecondCount.set(0);

                eventBus.post(event);
            }
        };

        executor.scheduleAtFixedRate(oneSecondTask, 1000l);
    }*/

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

    /*

    @SuppressWarnings("UnusedDeclaration") //called by the EventBus via reflection
    @Subscribe
    public void onRequestEvent(RequestEvent event) {
        requestCount.incrementAndGet();
        oneSecondCount.incrementAndGet();
        requestMeter.mark();
        sendEvents = true;
    }

    /*
    @SuppressWarnings("unchecked")
    private String toDatadogJson(String name, long millis, Object value) {

        Map item = new LinkedHashMap();
        item.put("metric", "darkstar." + name + ".metric");

        List points = new ArrayList();
        points.add(millis);
        points.add(value);

        item.put("points", Arrays.asList(points));
        item.put("type", "gauge");
        item.put("host", "darkstar.io");
        item.put("tags", Arrays.asList("environment:darkstar"));

        Map body = new LinkedHashMap<>();
        body.put("series", Arrays.asList(item));


        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    } */
}
