package io.darkstar.lb;

import io.darkstar.health.HealthMonitor;

import java.util.Collection;

public interface Pool {

    /**
     * Returns a convenient name for the group of origin servers, for example, {@code us-east-1}
     * or {@code Webapp Servers}.
     *
     * @return a convenient name for the group of origin servers.
     */
    String getName();

    /**
     * Returns the health monitors used to discover healthy origin servers in the group.
     *
     * @return the health monitors used to discover healthy origin servers in the group.
     */
    Collection<HealthMonitor> getHealthMonitors();

    /**
     * Returns the pool's origin server enlistments.
     *
     * @return the pool's origin server enlistments.
     */
    Collection<Enlistment> getEnlistments();
}
