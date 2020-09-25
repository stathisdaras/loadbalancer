package org.efstathiosdaras.swissre.loadbalancer.healthcheck;

import org.efstathiosdaras.swissre.loadbalancer.enumeration.HealthStatus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.efstathiosdaras.swissre.loadbalancer.enumeration.HealthStatus.ALIVE;
import static org.efstathiosdaras.swissre.loadbalancer.enumeration.HealthStatus.DEAD;

/**
 * Advanced service for checking a node's health status.
 * This component is a wrapper (decorator) around the {@link SimpleHeartBeatChecker} adding functionality to it.
 *
 * @author Stathis Daras
 */
public class AdvancedHeartBeatChecker implements HeartBeatChecker {

    private static final Integer CONSECUTIVE_ADMITTANCES_THRESHOLD = 2;

    /**
     * {@link SimpleHeartBeatChecker} decorated object.
     */
    private SimpleHeartBeatChecker heartBeatChecker = new SimpleHeartBeatChecker();

    /**
     * Mapping between nodeIds that have been excluded & the number of their successful heart beats (admittances).
     */
    protected final Map<String, Integer> admittancesPerExcludedNode = new ConcurrentHashMap<>();

    /**
     * Invokes simple health checker.
     * If down adds node to the excluded nodes for, resetting admittances.
     * If up & excluded, checks successful heartbeats (admittances), before including the node again.
     *
     * @param nodeId unique node identifier
     * @return health status
     */
    public HealthStatus check(String nodeId) {
        HealthStatus status = heartBeatChecker.check(nodeId);

        if (DEAD.equals(status)) {
            admittancesPerExcludedNode.put(nodeId, 0);
        } else {
            if (admittancesPerExcludedNode.containsKey(nodeId)) {
                return checkConsecutiveAdmittances(nodeId);
            }

        }
        return DEAD;
    }

    private HealthStatus checkConsecutiveAdmittances(String nodeId) {
        Integer admittances = admittancesPerExcludedNode.get(nodeId);

        if (admittances >= CONSECUTIVE_ADMITTANCES_THRESHOLD) {
            admittancesPerExcludedNode.remove(nodeId);
            return ALIVE;
        }

        admittancesPerExcludedNode.put(nodeId, ++admittances);
        return DEAD;
    }

    public Map<String, Integer> getAdmittancesPerExcludedNode() {
        return admittancesPerExcludedNode;
    }
}
