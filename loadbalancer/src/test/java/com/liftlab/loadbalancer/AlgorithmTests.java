package com.liftlab.loadbalancer;

import com.liftlab.loadbalancer.algorithms.RandomAlgorithm;
import com.liftlab.loadbalancer.algorithms.RoundRobinAlgorithm;
import com.liftlab.loadbalancer.fixtures.ServerFixtures;
import com.liftlab.loadbalancer.models.BackendServer;
import com.liftlab.loadbalancer.services.ServerRegistryService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class AlgorithmTests extends ServerFixtures {

    @Mock
    private ServerRegistryService serverRegistryService;

    private RoundRobinAlgorithm roundRobinAlgorithm;
    private RandomAlgorithm randomAlgorithm;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        serverRegistryService.registerServer(backendServer1);
        serverRegistryService.registerServer(backendServer2);
        serverRegistryService.registerServer(backendServer3);
        serverRegistryService.registerServer(backendServer4);
        serverRegistryService.registerServer(backendServer5);

        roundRobinAlgorithm = new RoundRobinAlgorithm();
        randomAlgorithm = new RandomAlgorithm();
    }

    @Test
    void testRoundRobinStrategy() {
        when(serverRegistryService.getAvailableServers()).thenReturn(backendServerList);

        // Simulate requests and check the selection
        Assertions.assertEquals(backendServer1.getUrl(), roundRobinAlgorithm.selectServer(serverRegistryService).getUrl());
        Assertions.assertEquals(backendServer2.getUrl(), roundRobinAlgorithm.selectServer(serverRegistryService).getUrl());
        Assertions.assertEquals(backendServer3.getUrl(), roundRobinAlgorithm.selectServer(serverRegistryService).getUrl());
        Assertions.assertEquals(backendServer1.getUrl(), roundRobinAlgorithm.selectServer(serverRegistryService).getUrl());  // After cycling
    }

    @Test
    void testRandomStrategy() {
        when(serverRegistryService.getAvailableServers()).thenReturn(backendServerList);

        // Test that a random server is selected
        BackendServer server1 = randomAlgorithm.selectServer(serverRegistryService);
        assertTrue(backendServerList.contains(server1));  // It should be one of the servers
    }

    @Test
    void testEmptyServerList() {
        when(serverRegistryService.getAvailableServers()).thenReturn(Collections.emptyList());

        assertNull(roundRobinAlgorithm.selectServer(serverRegistryService));
        assertNull(randomAlgorithm.selectServer(serverRegistryService));
    }
}
