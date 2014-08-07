package io.darkstar.health;

public interface HealthMonitor {

    /**
     * Returns the destination port to connect to when attempting a health check.
     *
     * @return the destination port to connect to when attempting a health check.
     */
    int getDestinationPort();

    /**
     * Returns the number of seconds between each health check.
     *
     * @return the number of seconds between each health check.
     */
    int getIntervalSeconds();

    /**
     * Returns the duration in milliseconds allowed to pass when waiting for an origin server to return a
     * response before the health check is considered a failure.  If the response is received before this
     * duration, the health check is considered a success.
     *
     * @return the duration in milliseconds allowed to pass when waiting for an origin server to return a
     *         response before the health check is considered a failure.
     */
    long getTimeoutThresholdMillis();

    /**
     * Returns the number of consecutive health checks that must succeed before a host is considered healthy and
     * able to receive traffic.  Once the number of consecutive successful health checks exceeds this number, the
     * host is considered healthy and will be seen as a viable candidate in load balancing algorithms.
     *
     * @return the number of consecutive health checks that must succeed before a host is considered healthy and
     *         able to receive traffic.
     */
    int getHealthyThresholdCount();

    /**
     * Returns the number of consecutive health checks that must fail before a host is considered unhealthy and
     * not able to receive traffic.  Once the number of consecutive failed health checks exceeds this number, the
     * host is considered unhealthy and will not be evaluated as a viable candidate in load balancing algorithms.
     *
     * @return the number of consecutive health checks that must fail before a host is considered unhealthy and
     *         not able to receive traffic.
     */
    int getUnhealthyThresholdCount();

}
