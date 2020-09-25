package org.efstathiosdaras.swissre.loadbalancer;

import org.efstathiosdaras.swissre.loadbalancer.algorithm.LoadBalancingAlgorithm;
import org.efstathiosdaras.swissre.loadbalancer.healthcheck.HeartBeatChecker;
import org.efstathiosdaras.swissre.loadbalancer.provider.Provider;
import org.efstathiosdaras.swissre.loadbalancer.provider.ProviderClusterService;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

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
     * Gets the result from provider.
     *
     * @return result
     */
    public String get() {
        return selectNode().get();
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
}
