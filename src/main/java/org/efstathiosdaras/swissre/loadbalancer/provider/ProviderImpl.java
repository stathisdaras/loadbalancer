package org.efstathiosdaras.swissre.loadbalancer.provider;

import java.util.UUID;

/**
 * Concrete implementation of the {@link Provider}.
 *
 * @author Stathis Daras
 */
public class ProviderImpl implements Provider {

    UUID id = UUID.randomUUID();

    /**
     * Returns a unique identifier ot the provider's instance.
     *
     * @return unique identifier
     */
    @Override
    public String get() {
        return String.valueOf(this.hashCode());
    }

    @Override
    public UUID getID() {
        return id;
    }

}
