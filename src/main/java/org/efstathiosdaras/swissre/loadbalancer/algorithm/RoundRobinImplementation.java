package org.efstathiosdaras.swissre.loadbalancer.algorithm;

import org.efstathiosdaras.swissre.loadbalancer.provider.Provider;

import java.util.List;

/**
 * Round Robin load balancer implementation.
 *
 * @author Stathis Daras
 */
public class RoundRobinImplementation implements LoadBalancingAlgorithm {

    private int position = 0;

    public Provider selectNode(List<Provider> providerNodes) {
        if (position > providerNodes.size() - 1) {
            position = 0;
        }

        Provider provider = providerNodes.get(position);
        position++;

        return provider;
    }
}
