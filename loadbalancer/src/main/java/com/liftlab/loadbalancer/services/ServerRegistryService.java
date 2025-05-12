package com.liftlab.loadbalancer.services;

import com.liftlab.loadbalancer.models.BackendServer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class ServerRegistryService {
    private static List<BackendServer> availableServers = Collections.synchronizedList(new ArrayList<>());

    public List<BackendServer> getAvailableServers() {
        return new ArrayList<>(availableServers);
    }

    public void registerServer(BackendServer server) {
        if (availableServers.stream().noneMatch(i -> i.getUrl().equalsIgnoreCase(server.getUrl()))) {
            availableServers.add(server);
        }
    }

    public void removeServer(BackendServer server) {
        availableServers.remove(server);
    }

    public boolean isServerAvailable(String url) {
        return availableServers.stream().anyMatch(i -> i.getUrl().equalsIgnoreCase(url));
    }

    public boolean changeHealth(String url, boolean healthy) {
        Optional<BackendServer> selectedServer=
                availableServers.stream().filter( server -> server.getUrl().equalsIgnoreCase(url))
                        .findFirst();

        if(selectedServer.isEmpty())
            return false;
        selectedServer.get().setHealthy(healthy);
        return true;
    }
}

