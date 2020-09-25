package org.efstathiosdaras.swissre.loadbalancer.algorithm;

import org.efstathiosdaras.swissre.loadbalancer.provider.Provider;

import java.util.List;
import java.util.Random;

/**
 * Random load balancer implementation.
 *
 * @author Stathis Daras
 */
public class RandomImplementation implements LoadBalancingAlgorithm {

    private final Random intRandom = new Random();

    public Provider selectNode(List<Provider> providerNodes) {
        int randomPosition = intRandom.nextInt(providerNodes.size());

        return providerNodes.get(randomPosition);
    }
}
