package org.efstathiosdaras.swissre.loadbalancer.provider;

import org.junit.Test;

import java.util.Optional;

import static org.efstathiosdaras.swissre.loadbalancer.enumeration.AdmittanceStatus.EXCLUDED;
import static org.efstathiosdaras.swissre.loadbalancer.enumeration.AdmittanceStatus.INCLUDED;
import static org.efstathiosdaras.swissre.loadbalancer.provider.ProviderClusterService.MAX_CLUSTER_SIZE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Unit test cases for {@link ProviderClusterService} class.
 *
 * @author Stathis Daras
 */
public class ProviderClusterServiceTest {

    @Test
    public void registerNodes_doesNotExceedMaxSize() {
        // Given
        int clusterSize = MAX_CLUSTER_SIZE + 1;

        // When
        ProviderClusterService clusterService = ProviderClusterService.getInstance(clusterSize);
        clusterService.registerNodes(clusterSize);

        // Then
        int actualClusterSize = clusterService.getNodesMap().size();
        assertEquals(MAX_CLUSTER_SIZE, actualClusterSize);
    }

    @Test
    public void nodeIncluded_statusUpdated() {
        // Given
        ProviderClusterService clusterService = ProviderClusterService.getInstance();

        // When
        Optional<String> nodeIdOptional = clusterService.getNodesMap().keySet().stream().findAny();

        if (nodeIdOptional.isPresent()) {
            String nodeId = nodeIdOptional.get();
            clusterService.excludeNode(nodeId);

            // Then
            ProviderStateDTO nodeState = clusterService.getNodesMap().get(nodeId);
            assertEquals(EXCLUDED, nodeState.getAdmittanceStatus());
        } else {
            fail("Node cluster is empty.");
        }
    }

    @Test
    public void nodeExcluded_statusUpdated() {
        // Given
        ProviderClusterService clusterService = ProviderClusterService.getInstance();

        // When
// When
        Optional<String> nodeIdOptional = clusterService.getNodesMap().keySet().stream().findAny();

        if (nodeIdOptional.isPresent()) {
            String nodeId = nodeIdOptional.get();
            clusterService.includeNode(nodeId);

            // Then
            ProviderStateDTO nodeState = clusterService.getNodesMap().get(nodeId);
            assertEquals(INCLUDED, nodeState.getAdmittanceStatus());

        } else {
            fail("Node cluster is empty.");
        }
    }

}
