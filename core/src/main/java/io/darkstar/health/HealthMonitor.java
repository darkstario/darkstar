package io.darkstar.health;

public interface HealthMonitor {

    /**
     * Returns the HTTP scheme that will be used when attempting a health check ping request (either {@code http}
     * or {@code https}).
     *
     * @return the HTTP scheme that will be used when attempting a health check ping request (either {@code http}
     *         or {@code https}).
     */
    String getRequestScheme();

    /**
     * Returns the HTTP port to connect to when attempting a health check ping request (usually {@code 80} for http or
     * {@code 443} for https).
     *
     * @return the HTTP port to connect to when attempting a health check ping request (usually {@code 80} for http or
     *         {@code 443} for https).
     */
    int getRequestPort();

    /**
     * Returns the path to request when attempting a health check ping request.  The default value is {@code /}.
     *
     * @return the path to request when attempting a health check ping request.
     */
    String getRequestPath();

    /**
     * Returns the number of seconds between each health check ping.
     *
     * @return the number of seconds between each health check ping.
     */
    int getIntervalSeconds();

    /**
     * Returns the duration in milliseconds allowed to pass when waiting for an origin server to return an http
     * response before the health check ping is considered a failure.  If the response is received before this
     * duration, the health check ping is considered a success.
     *
     * @return the duration in milliseconds allowed to pass when waiting for an origin server to return an http
     *         response before the health check ping attempt is considered a failure.
     */
    long getTimeoutThresholdMillis();

    /**
     * Returns the number of consecutive health check pings that must succeed before a host is considered healthy and
     * able to receive traffic.  Once the number of consecutive successful health check pings exceeds this number, the
     * host is considered healthy and will be seen as a viable candidate in load balancing algorithms.
     *
     * @return the number of consecutive health check pings that must succeed before a host is considered healthy and
     *         able to receive traffic.
     */
    int getHealthyThresholdCount();

    /**
     * Returns the number of consecutive health check pings that must fail before a host is considered unhealthy and
     * not able to receive traffic.  Once the number of consecutive failed health check pings exceeds this number, the
     * host is considered unhealthy and will not be evaluated as a viable candidate in load balancing algorithms.
     *
     * @return the number of consecutive health check pings that must fail before a host is considered unhealthy and
     *         not able to receive traffic.
     */
    int getUnhealthyThresholdCount();

}
