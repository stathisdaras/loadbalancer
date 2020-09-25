package org.efstathiosdaras.swissre.loadbalancer.provider;

/**
 * Concrete implementation of the {@link Provider}.
 *
 * @author Stathis Daras
 */
public class ProviderImpl implements Provider {

    /**
     * Returns a unique identifier ot the provider's instance.
     *
     * @return unique identifier
     */
    @Override
    public String get() {
        return String.valueOf(this.hashCode());
    }
}
