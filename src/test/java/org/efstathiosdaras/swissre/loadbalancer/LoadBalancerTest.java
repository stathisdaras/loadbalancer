package org.efstathiosdaras.swissre.loadbalancer;

import org.efstathiosdaras.swissre.loadbalancer.algorithm.RandomImplementation;
import org.efstathiosdaras.swissre.loadbalancer.algorithm.RoundRobinImplementation;
import org.efstathiosdaras.swissre.loadbalancer.healthcheck.SimpleHeartBeatChecker;
import org.efstathiosdaras.swissre.loadbalancer.provider.ProviderClusterService;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

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
        List<String> results = ProviderClusterService.getInstance().getNodesMap().values().stream()
                .map(e -> e.getNode().get()).collect(Collectors.toList());

        // Then , all nodeIds are sequentially selected
        // In 2nd iteration round-robin index is reset
        for (int i = 0; i < 2; i++) {
            results.forEach(result -> {
                String selectedNodeId = balancer.get();
                assertNotNull(selectedNodeId);
                assertEquals(selectedNodeId, result);
            });
        }
    }

    /**
     * Rate limiter test
     */
    @Test
    public void maxCapacityReached_errorsPresent() {
        LoadBalancer balancer = LoadBalancer.getInstance(new RandomImplementation(), new SimpleHeartBeatChecker());
        int clusterSize = 2;
        ProviderClusterService.getInstance(clusterSize);

        ExecutorService serviceExec = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 100_000 * clusterSize; i++) {
            serviceExec.submit(balancer::get);
        }

        assertNotEquals(0, balancer.getErrors().get());
    }

    @Test
    public void expiredRequests_willBeRemoved() {
        // Given
        UUID nodeId = UUID.randomUUID();
        LoadBalancer balancer = LoadBalancer.getInstance(new RoundRobinImplementation(), new SimpleHeartBeatChecker());
        Instant anHourBefore = Instant.now().minus(1, ChronoUnit.HOURS);

        List<Instant> requestInstants = new ArrayList<>();
        requestInstants.add(Instant.now());
        requestInstants.add(anHourBefore);
        balancer.getParallelRequestsPerNode().put(nodeId, requestInstants);

        // When
        balancer.removeExpiredRequests();

        // Then
        List<Instant> instants = balancer.getParallelRequestsPerNode().get(nodeId);
        assertFalse(instants.contains(anHourBefore));
    }
}
