package org.efstathiosdaras.swissre.loadbalancer.provider;

/**
 * Interface defining methods of Service Provider.
 *
 * @author Stathis Daras
 */
public interface Provider {

    /**
     * Returns a unique identifier ot the provider's instance.
     *
     * @return identifier
     */
    String get();
}
