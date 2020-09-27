package org.efstathiosdaras.swissre.loadbalancer;

import org.efstathiosdaras.swissre.loadbalancer.algorithm.LoadBalancingAlgorithm;
import org.efstathiosdaras.swissre.loadbalancer.healthcheck.HeartBeatChecker;
import org.efstathiosdaras.swissre.loadbalancer.provider.Provider;
import org.efstathiosdaras.swissre.loadbalancer.provider.ProviderClusterService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private ConcurrentHashMap<UUID, List<Instant>> parallelRequestsPerNode = new ConcurrentHashMap<>();

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
        List<Instant> requestInstants = parallelRequestsPerNode.get(nodeId);

        // if node has free capacity
        if (requestInstants == null || requestInstants.size() < MAX_REQUESTS_PER_NODE) {
            return handleNodeRequest(selectedNode, nodeId);
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

    /**
     * Add request to parallel requests
     * process request &
     * finally remove from parallel requests
     * & remove all expired requests
     */
    private String handleNodeRequest(Provider selectedNode, UUID nodeId) {
        Instant now = Instant.now();
        String result;

        addToNodeRequests(nodeId, now);
        try {
            result = selectedNode.get(); // this process could be long-running
        } finally {
            subtractFromNodeRequests(nodeId, now);
            removeExpiredRequests();
        }

        return result;
    }

    /**
     * For every node, detects the requests that have expired & cleans them
     * By expired, meaning requests that lived in the hashmap more than REQUEST_TTL_MIN
     */
    protected void removeExpiredRequests() {
        parallelRequestsPerNode.forEach((nodeId, instants) -> {
            for (int i = 0; i < instants.size(); i++) {
                Instant instant = instants.get(i);
                Instant lastHour = Instant.now().minus(1, ChronoUnit.HOURS);
                if (instant.isBefore(lastHour)) {
                    instants.remove(instant);
                }

            }
        });
    }

    private boolean allNodesBusy() {
        return parallelRequestsPerNode.entrySet().stream()
                .noneMatch(e -> e.getValue().size() < MAX_REQUESTS_PER_NODE);
    }

    private void subtractFromNodeRequests(UUID nodeId, Instant instant) {
        List<Instant> requestInstants = parallelRequestsPerNode.get(nodeId);
        if (requestInstants != null) {
            requestInstants.remove(instant);
            parallelRequestsPerNode.put(nodeId, requestInstants);
        }
    }

    private void addToNodeRequests(UUID nodeId, Instant instant) {
        List<Instant> requestInstants = parallelRequestsPerNode.get(nodeId);
        if (requestInstants == null) {
            requestInstants = new ArrayList<>();
        }

        requestInstants.add(instant);
        parallelRequestsPerNode.put(nodeId, requestInstants);
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

    public Map<UUID, List<Instant>> getParallelRequestsPerNode() {
        return parallelRequestsPerNode;
    }
}
