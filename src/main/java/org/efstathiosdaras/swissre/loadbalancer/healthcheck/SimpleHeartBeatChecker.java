package org.efstathiosdaras.swissre.loadbalancer.healthcheck;

import org.efstathiosdaras.swissre.loadbalancer.enumeration.HealthStatus;
import org.efstathiosdaras.swissre.loadbalancer.provider.ProviderClusterService;
import org.efstathiosdaras.swissre.loadbalancer.provider.ProviderStateDTO;

import java.util.Map;
import java.util.UUID;

import static org.efstathiosdaras.swissre.loadbalancer.enumeration.HealthStatus.DEAD;

/**
 * Simple component for checking nodes' health status.
 *
 * @author Stathis Daras
 */
public class SimpleHeartBeatChecker implements HeartBeatChecker {

    private ProviderClusterService nodesService = ProviderClusterService.getInstance();

    /**
     * Retrieves node, if node is alive is excluded, else included.
     *
     * @param nodeId unique node identifier
     * @return health status
     */
    public HealthStatus check(UUID nodeId) {
        Map<UUID, ProviderStateDTO> providersMap = nodesService.getNodesMap();
        ProviderStateDTO nodeState = providersMap.get(nodeId);
        HealthStatus status = nodeState.getHealthStatus();

        if (DEAD.equals(status)) {
            nodesService.excludeNode(nodeId);
        } else {
            nodesService.includeNode(nodeId);
        }

        return status;
    }

}
