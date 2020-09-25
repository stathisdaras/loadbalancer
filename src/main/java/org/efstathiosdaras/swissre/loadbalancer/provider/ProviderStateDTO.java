package org.efstathiosdaras.swissre.loadbalancer.provider;

import org.efstathiosdaras.swissre.loadbalancer.enumeration.AdmittanceStatus;
import org.efstathiosdaras.swissre.loadbalancer.enumeration.HealthStatus;

import static org.efstathiosdaras.swissre.loadbalancer.enumeration.AdmittanceStatus.INCLUDED;
import static org.efstathiosdaras.swissre.loadbalancer.enumeration.HealthStatus.ALIVE;

/**
 * Wrapper object referencing provider instance & holding it's state.
 *
 * @author Stathis Daras
 */
public class ProviderStateDTO {
    /**
     * Whether a node is included or not in the cluster.
     */
    private AdmittanceStatus admittanceStatus = INCLUDED;

    /**
     * Alive/dead status of node.
     */
    private HealthStatus healthStatus = ALIVE;

    /**
     * Reference to the node instance.
     */
    private Provider node;

    public ProviderStateDTO(Provider node) {
        this.node = node;
    }

    public AdmittanceStatus getAdmittanceStatus() {
        return admittanceStatus;
    }

    public void setAdmittanceStatus(AdmittanceStatus admittanceStatus) {
        this.admittanceStatus = admittanceStatus;
    }

    public Provider getNode() {
        return node;
    }

    public void setNode(Provider node) {
        this.node = node;
    }

    public HealthStatus getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(HealthStatus healthStatus) {
        this.healthStatus = healthStatus;
    }
}
