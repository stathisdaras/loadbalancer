package org.efstathiosdaras.swissre.loadbalancer.provider;

import org.efstathiosdaras.swissre.loadbalancer.enumeration.AdmittanceStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.efstathiosdaras.swissre.loadbalancer.enumeration.AdmittanceStatus.EXCLUDED;
import static org.efstathiosdaras.swissre.loadbalancer.enumeration.AdmittanceStatus.INCLUDED;

/**
 * Singleton service responsible for managing provider nodes.
 *
 * @author Stathis Daras
 */
public class ProviderClusterService {
    private static final int DEFAULT_CLUSTER_SIZE = 2;
    protected static final int MAX_CLUSTER_SIZE = 10;

    private static ProviderClusterService instance;
    protected final Map<UUID, ProviderStateDTO> nodesMap = new ConcurrentHashMap<>();

    private ProviderClusterService(int clusterSize) {
        for (int i = 0; i < clusterSize - 1; i++) {
            registerNode(new ProviderImpl());
        }
    }

    public static ProviderClusterService getInstance(int clusterSize) {
        if (instance == null) {
            instance = new ProviderClusterService(clusterSize);
        }

        return instance;
    }

    public static ProviderClusterService getInstance() {
        return getInstance(DEFAULT_CLUSTER_SIZE);
    }

    /**
     * Registers node to the cluster.
     *
     * @param provider instance node
     */
    public void registerNode(ProviderImpl provider) {
        if (nodesMap.size() < MAX_CLUSTER_SIZE) {
            nodesMap.put(provider.getID(), new ProviderStateDTO(provider));
        }
    }

    /**
     * Updates selected node's admittance status to EXCLUDED.
     */
    public void excludeNode(UUID nodeId) {
        nodesMap.get(nodeId).setAdmittanceStatus(EXCLUDED);
    }

    /**
     * Updates selected node's admittance status to INCLUDED.
     */
    public void includeNode(UUID nodeId) {
        nodesMap.get(nodeId).setAdmittanceStatus(INCLUDED);
    }

    /**
     * Filters node by their {@link AdmittanceStatus}.
     *
     * @param admittanceStatus status
     * @return Provider node instance
     */
    public List<Provider> getNodesByAdmittanceStatus(AdmittanceStatus admittanceStatus) {
        return nodesMap.values().stream()
                .filter(node -> admittanceStatus.equals(node.getAdmittanceStatus()))
                .map(ProviderStateDTO::getNode)
                .collect(Collectors.toList());
    }

    public Map<UUID, ProviderStateDTO> getNodesMap() {
        return nodesMap;
    }

    public void registerNodes(int clusterSIze) {
        nodesMap.clear();

        for (int i = 0; i < clusterSIze; i++) {
            registerNode(new ProviderImpl());
        }
    }
}
