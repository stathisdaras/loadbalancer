package org.efstathiosdaras.swissre.loadbalancer.provider;

import java.util.UUID;

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

    UUID getID();
}
