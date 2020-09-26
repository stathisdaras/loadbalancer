package org.efstathiosdaras.swissre.loadbalancer;

import org.efstathiosdaras.swissre.loadbalancer.algorithm.LoadBalancingAlgorithm;
import org.efstathiosdaras.swissre.loadbalancer.healthcheck.HeartBeatChecker;
import org.efstathiosdaras.swissre.loadbalancer.provider.Provider;
import org.efstathiosdaras.swissre.loadbalancer.provider.ProviderClusterService;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.Executors.newScheduledThreadPool;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.efstathiosdaras.swissre.loadbalancer.enumeration.AdmittanceStatus.INCLUDED;

/**
 * Load balancer component.
 *
 * @author Stathis Daras
 */
public class LoadBalancer {
    private static LoadBalancer instance;
    private static final long HEALTH_CHECK_PERIOD_SEC = 5L;

    private static final ProviderClusterService nodesService = ProviderClusterService.getInstance();
    private final LoadBalancingAlgorithm lbAlgorithm;
    private final HeartBeatChecker heartBeatChecker;

    private static final int MAX_REQUESTS_PER_NODE = 10;
    private ConcurrentHashMap<UUID, Integer> parallelRequestsPerNode = new ConcurrentHashMap<>();

    // for monitoring / test purposes
    private AtomicInteger errors = new AtomicInteger();

    private LoadBalancer(LoadBalancingAlgorithm lbAlgorithm, HeartBeatChecker heartBeatChecker) {
        this.lbAlgorithm = lbAlgorithm;
        this.heartBeatChecker = heartBeatChecker;
        scheduleHealthCheck(newScheduledThreadPool(1));
    }

    public static LoadBalancer getInstance(LoadBalancingAlgorithm lbAlgorithm, HeartBeatChecker heartBeatChecker) {
        if (instance == null) {
            instance = new LoadBalancer(lbAlgorithm, heartBeatChecker);
        }

        return instance;
    }

    /**
     * Gets the result from provider cluster.
     *
     * @return result
     */
    public String get() {
        // Retrieve node
        Provider selectedNode = selectNode();
        UUID nodeId = selectedNode.getID();
        Integer issuedRequests = parallelRequestsPerNode.get(nodeId);

        // if node has free capacity
        // add request to parallel requests
        // process request & remove from parallel requests
        if (issuedRequests == null || issuedRequests < MAX_REQUESTS_PER_NODE) {

            incrementNodeRequests(nodeId);
            String identifier = selectedNode.get(); // this process could be long-running
            decrementNodeRequests(nodeId);

            return identifier;
            // total capacity limit is reached (all available nodes have reached their capacity)
            // then communicate error
        } else if (allNodesBusy()) {
            errors.incrementAndGet();
            throw new IllegalStateException("All cluster nodes are busy.");
            // if node is busy, try again recursively
        } else {
            return get();
        }
    }

    private boolean allNodesBusy() {
        return parallelRequestsPerNode.entrySet().stream()
                .noneMatch(e -> e.getValue() < MAX_REQUESTS_PER_NODE);
    }

    private void decrementNodeRequests(UUID nodeId) {
        Integer issuedRequests = parallelRequestsPerNode.get(nodeId);
        if (issuedRequests == 1) {
            parallelRequestsPerNode.remove(nodeId);
        } else {
            parallelRequestsPerNode.put(nodeId, --issuedRequests);
        }
    }

    private void incrementNodeRequests(UUID nodeId) {
        Integer issuedRequests = parallelRequestsPerNode.get(nodeId);
        if (issuedRequests == null) {
            parallelRequestsPerNode.put(nodeId, 1);
        } else {
            parallelRequestsPerNode.put(nodeId, ++issuedRequests);
        }
    }

    /**
     * Retrieves available nodes & makes the selection based on the configured algorithm.
     *
     * @return selected provider node.
     */
    public Provider selectNode() {
        List<Provider> includedNodes = nodesService.getNodesByAdmittanceStatus(INCLUDED);

        return lbAlgorithm.selectNode(includedNodes);
    }

    /**
     * Schedules an asynchronous task of checking nodes' health every 5 sec
     * using {@link ScheduledExecutorService}.
     *
     * @param heartBeatExecutor injected heart beat checker
     */
    private void scheduleHealthCheck(ScheduledExecutorService heartBeatExecutor) {
        heartBeatExecutor.schedule(
                () -> nodesService.getNodesMap().keySet().forEach(heartBeatChecker::check),
                HEALTH_CHECK_PERIOD_SEC,
                SECONDS
        );
    }

    /**
     * Testing / monitoring
     */
    public AtomicInteger getErrors() {
        return errors;
    }
}
