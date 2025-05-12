package com.liftlab.loadbalancer.algorithms;

import com.liftlab.loadbalancer.models.BackendServer;
import com.liftlab.loadbalancer.services.ServerRegistryService;

import java.util.List;
import java.util.Random;

import static com.liftlab.loadbalancer.ApplicationConstants.RANDOM_ALGORITHM_NAME;

public class RandomAlgorithm implements LoadBalancingAlgorithm{
    private final Random random = new Random();

    @Override
    public String getAlgorithmName(){
        return RANDOM_ALGORITHM_NAME;
    }

    @Override
    public BackendServer selectServer(ServerRegistryService serverRegistryService) {
        List<BackendServer> servers = serverRegistryService.getAvailableServers();
        if (servers.isEmpty()) return null;
        return servers.get(random.nextInt(servers.size()));
    }
}
