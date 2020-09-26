package org.efstathiosdaras.swissre.loadbalancer.healthcheck;

import org.efstathiosdaras.swissre.loadbalancer.provider.ProviderClusterService;
import org.efstathiosdaras.swissre.loadbalancer.provider.ProviderImpl;
import org.efstathiosdaras.swissre.loadbalancer.provider.ProviderStateDTO;
import org.junit.Test;

import java.util.UUID;

import static org.efstathiosdaras.swissre.loadbalancer.enumeration.AdmittanceStatus.EXCLUDED;
import static org.efstathiosdaras.swissre.loadbalancer.enumeration.AdmittanceStatus.INCLUDED;
import static org.efstathiosdaras.swissre.loadbalancer.enumeration.HealthStatus.DEAD;
import static org.junit.Assert.assertEquals;

/**
 * Unit test cases for {@link SimpleHeartBeatChecker} class.
 *
 * @author Stathis Daras
 */
public class SimpleHeartBeatCheckerTest {

    private ProviderClusterService clusterService = ProviderClusterService.getInstance();

    @Test
    public void nodeDown_willBeMarkedExcluded() {

        // Given
        UUID deadNodeId = UUID.randomUUID();
        SimpleHeartBeatChecker checker = new SimpleHeartBeatChecker();
        clusterService.getNodesMap().put(deadNodeId, createDeadNode());

        // When
        checker.check(deadNodeId);

        // Then
        ProviderStateDTO nodeState = clusterService.getNodesMap().get(deadNodeId);
        assertEquals(EXCLUDED, nodeState.getAdmittanceStatus());
    }

    @Test
    public void nodeUp_willBeMarkedIncluded() {
        // Given
        SimpleHeartBeatChecker checker = new SimpleHeartBeatChecker();

        UUID aliveNodeId = UUID.randomUUID();
        clusterService.getNodesMap().put(aliveNodeId, new ProviderStateDTO(new ProviderImpl()));

        // When
        checker.check(aliveNodeId);

        // Then
        ProviderStateDTO nodeState = clusterService.getNodesMap().get(aliveNodeId);
        assertEquals(INCLUDED, nodeState.getAdmittanceStatus());
    }

    private ProviderStateDTO createDeadNode() {
        ProviderStateDTO nodeState = new ProviderStateDTO(new ProviderImpl());
        nodeState.setHealthStatus(DEAD);
        return nodeState;
    }

}
