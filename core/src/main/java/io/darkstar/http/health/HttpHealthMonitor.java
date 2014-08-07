package io.darkstar.http.health;

import io.darkstar.health.HealthMonitor;

public interface HttpHealthMonitor extends HealthMonitor {

    /**
     * Returns the HTTP scheme that will be used when attempting a health check request (either {@code http}
     * or {@code https}).
     *
     * @return the HTTP scheme that will be used when attempting a health check request (either {@code http}
     *         or {@code https}).
     */
    String getRequestScheme();

    /**
     * Returns the URI path to use when attempting a health check request.  The default value is {@code /}.
     *
     * @return the URI path to use when attempting a health check request.
     */
    String getRequestPath();
}
