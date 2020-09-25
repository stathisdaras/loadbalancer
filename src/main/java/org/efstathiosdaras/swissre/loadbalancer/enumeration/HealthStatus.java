package org.efstathiosdaras.swissre.loadbalancer.enumeration;

/**
 * Enum representation of the 2 health states a node can be allowed to have.
 *
 * @author Stathis Daras
 */
public enum HealthStatus {
    ALIVE, // node is alive
    DEAD // node is dead :(
}
