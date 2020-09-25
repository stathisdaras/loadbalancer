package org.efstathiosdaras.swissre.loadbalancer.healthcheck;

import org.efstathiosdaras.swissre.loadbalancer.enumeration.HealthStatus;

/**
 * Interface defining the behavior of node health checking components.
 *
 * @author Stathis Daras
 */
public interface HeartBeatChecker {

    /**
     * Checks if node is dead or alive.
     *
     * @param nodeId unique node identifier
     * @return health status enum {@link HealthStatus}
     */
    HealthStatus check(String nodeId);
}
