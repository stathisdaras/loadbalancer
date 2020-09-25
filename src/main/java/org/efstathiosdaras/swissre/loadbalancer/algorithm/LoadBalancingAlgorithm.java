package org.efstathiosdaras.swissre.loadbalancer.algorithm;

import org.efstathiosdaras.swissre.loadbalancer.provider.Provider;

import java.util.List;

public interface LoadBalancingAlgorithm {

    /**
     * Selects a node applying the current load balancing algorithm.
     *
     * @param providerNodes list of available nodes
     * @return selected provider
     */
    Provider selectNode(List<Provider> providerNodes);
}
