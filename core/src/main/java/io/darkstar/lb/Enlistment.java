package io.darkstar.lb;

import io.darkstar.health.HealthStatus;
import io.darkstar.net.Host;

import java.time.ZonedDateTime;

/**
 * An enlistment represents the association of an origin server host with a load balancing pool.  When an origin server
 * is added to a pool, an enlistment is created.  When an origin server is removed from a pool, its respective
 * enlistment is deleted.
 */
public interface Enlistment {

    /**
     * Returns the origin server host added to the associated load balancing {@link #getPool() pool}.
     *
     * @return the origin server host added to the associated load balancing {@link #getPool() pool}.
     */
    Host getHost();

    /**
     * Returns a load balancing pool that contains the associated origin server {@link #getHost() host}.
     *
     * @return a load balancing pool that contains the associated origin server {@link #getHost() host}.
     */
    Pool getPool();

    /**
     * Returns the current health status of the associated {@link #getHost() host}.
     *
     * @return the current health status of the associated {@link #getHost() host}.
     */
    HealthStatus getHealthStatus();

    /**
     * Convenience method that returns {@code true} if and only if the current {@link #getHealthStatus() healthStatus}
     * is {@link HealthStatus#HEALTHY}, {@code false} otherwise.
     *
     * @return {@code true} if and only if the current {@link #getHealthStatus() healthStatus} is
     *         {@link HealthStatus#HEALTHY}, {@code false} otherwise.
     */
    boolean isHealthy();

    /**
     * The timestamp when the {@link #getHealthStatus() healthStatus} was set to its current value.
     *
     * @return the timestamp when the {@link #getHealthStatus() healthStatus} was set to its current value.
     */
    ZonedDateTime getHealthStatusTimestamp();
}
