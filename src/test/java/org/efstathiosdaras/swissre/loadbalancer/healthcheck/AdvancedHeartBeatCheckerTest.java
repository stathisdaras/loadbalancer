package org.efstathiosdaras.swissre.loadbalancer.healthcheck;

import org.efstathiosdaras.swissre.loadbalancer.provider.ProviderClusterService;
import org.efstathiosdaras.swissre.loadbalancer.provider.ProviderImpl;
import org.efstathiosdaras.swissre.loadbalancer.provider.ProviderStateDTO;
import org.junit.Test;

import java.util.Map;
import java.util.UUID;

import static org.efstathiosdaras.swissre.loadbalancer.enumeration.AdmittanceStatus.EXCLUDED;
import static org.efstathiosdaras.swissre.loadbalancer.enumeration.HealthStatus.DEAD;
import static org.junit.Assert.assertEquals;

/**
 * Unit test cases for {@link AdvancedHeartBeatChecker} class.
 *
 * @author Stathis Daras
 */
public class AdvancedHeartBeatCheckerTest {

    private ProviderClusterService clusterService = ProviderClusterService.getInstance();

    @Test
    public void nodeDown_willBeStoredInExcluded() {
        // Given
        AdvancedHeartBeatChecker checker = new AdvancedHeartBeatChecker();

        UUID deadNodeId = UUID.randomUUID();
        clusterService.getNodesMap().put(deadNodeId, createDeadNode());

        // When
        checker.check(deadNodeId);

        // Then
        Map<UUID, Integer> perExcludedNode = checker.getAdmittancesPerExcludedNode();
        Integer admittances = perExcludedNode.get(deadNodeId);
        assertEquals(0, admittances.intValue());
    }

    private ProviderStateDTO createDeadNode() {
        ProviderStateDTO nodeState = new ProviderStateDTO(new ProviderImpl());
        nodeState.setHealthStatus(DEAD);
        return nodeState;
    }

}
