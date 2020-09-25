package org.efstathiosdaras.swissre.loadbalancer;

import org.efstathiosdaras.swissre.loadbalancer.algorithm.RandomImplementation;
import org.efstathiosdaras.swissre.loadbalancer.algorithm.RoundRobinImplementation;
import org.efstathiosdaras.swissre.loadbalancer.healthcheck.SimpleHeartBeatChecker;
import org.efstathiosdaras.swissre.loadbalancer.provider.ProviderClusterService;
import org.efstathiosdaras.swissre.loadbalancer.provider.ProviderStateDTO;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit test cases for {@link LoadBalancer} class.
 *
 * @author Stathis Daras
 */
public class LoadBalancerTest {

    @Test
    public void randomAlgorithm_Success() {
        // Given
        LoadBalancer balancer = LoadBalancer.getInstance(new RandomImplementation(), new SimpleHeartBeatChecker());

        // When
        String selectedNodeId = balancer.get();

        // Then
        assertNotNull(selectedNodeId);
    }

    @Test
    public void roundRobinAlgorithm_Success() {
        // Given
        LoadBalancer balancer = LoadBalancer.getInstance(new RoundRobinImplementation(), new SimpleHeartBeatChecker());

        // When
        Map<String, ProviderStateDTO> nodesMap = ProviderClusterService.getInstance().getNodesMap();

        // Then , all nodeIds are sequentially selected
        // In 2nd iteration round-robin index is reset
        for (int i = 0; i < 2; i++) {
            nodesMap.keySet().forEach(nodeId -> {
                String selectedNodeId = balancer.get();
                assertNotNull(selectedNodeId);
                assertEquals(selectedNodeId, nodeId);
            });
        }
    }
}
