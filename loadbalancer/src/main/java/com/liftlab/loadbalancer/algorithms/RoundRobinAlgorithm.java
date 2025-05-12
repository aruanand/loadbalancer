package com.liftlab.loadbalancer.algorithms;

import com.liftlab.loadbalancer.models.BackendServer;
import com.liftlab.loadbalancer.services.ServerRegistryService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.liftlab.loadbalancer.ApplicationConstants.ROUND_ROBIN_ALGORITHM_NAME;

@Component
public class RoundRobinAlgorithm implements LoadBalancingAlgorithm {
    private final AtomicInteger index = new AtomicInteger(0);

    @Override
    public String getAlgorithmName() {
        return ROUND_ROBIN_ALGORITHM_NAME;
    }

    @Override
    public BackendServer selectServer(ServerRegistryService serverRegistryService) {
        List<BackendServer> servers = serverRegistryService.getAvailableServers();
        if (servers.isEmpty()) return null;
        int pos = Math.abs(index.getAndIncrement() % servers.size());
        return servers.get(pos);
    }
}
