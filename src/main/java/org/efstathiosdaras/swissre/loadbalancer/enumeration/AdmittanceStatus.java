package org.efstathiosdaras.swissre.loadbalancer.enumeration;

/**
 * Enum representation of the 2 admittance states a node can be allowed to have.
 *
 * @author Stathis Daras
 */
public enum AdmittanceStatus {
    EXCLUDED, // node will not be available for selection
    INCLUDED // node will be available for selection
}
