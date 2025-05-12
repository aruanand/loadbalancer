package com.liftlab.loadbalancer.schedulers;

import com.liftlab.loadbalancer.models.BackendServer;
import com.liftlab.loadbalancer.services.ServerRegistryService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
class HealthChecker {
    private final ServerRegistryService serverRegistryService;
    private final RestTemplate restTemplate;

    private static final Logger LOGGER = LogManager.getLogger(HealthChecker.class);

    @Autowired
    public HealthChecker(ServerRegistryService serverRegistryService, RestTemplate restTemplate) {
        this.serverRegistryService = serverRegistryService;
        this.restTemplate = restTemplate;
    }

    @Scheduled(cron="0 */1 * * * *")
    public void checkServers() {
        LOGGER.info("Starting server health check");
        List<BackendServer> registeredServers = serverRegistryService.getAvailableServers();
        if (registeredServers.isEmpty())
            LOGGER.info("No server registered yet");
        for (BackendServer server : registeredServers) {
            try {
                // TODO: uncomment this to check live server health
//                restTemplate.getForEntity(server + "/actuator/health", String.class);
                if(server.isHealthy()) {
                    LOGGER.info("{} is UP!", server.getUrl());
                    return;
                }
                serverRegistryService.removeServer(server);
                LOGGER.info("{} has gone DOWN!", server.getUrl());
            } catch (Exception e) {
                serverRegistryService.removeServer(server);
                LOGGER.info("{} has gone DOWN!", server.getUrl());
            }
        }
    }
}