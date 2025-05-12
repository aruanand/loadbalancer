package com.liftlab.loadbalancer.algorithms;

import static com.liftlab.loadbalancer.ApplicationConstants.*;
import com.liftlab.loadbalancer.models.BackendServer;
import com.liftlab.loadbalancer.services.ServerRegistryService;

import java.util.List;
import java.util.NoSuchElementException;

public interface LoadBalancingAlgorithm {
    BackendServer selectServer(ServerRegistryService serverRegistryService);
    String getAlgorithmName();

    static LoadBalancingAlgorithm getAlgorithm(String name) {
        switch (name){
            case ROUND_ROBIN_ALGORITHM_NAME:
                return new RoundRobinAlgorithm();
            case RANDOM_ALGORITHM_NAME:
                return new RandomAlgorithm();
        }
        throw new NoSuchElementException(name + " is not supported");
    }

    static List<LoadBalancingAlgorithm> getStrategyList(){
        return List.of(
                new RandomAlgorithm(),
                new RoundRobinAlgorithm()
        );
    }
}
