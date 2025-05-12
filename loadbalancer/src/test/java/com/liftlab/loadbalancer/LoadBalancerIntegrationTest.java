package com.liftlab.loadbalancer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liftlab.loadbalancer.fixtures.ServerFixtures;
import com.liftlab.loadbalancer.models.ListResponseEntity;
import com.liftlab.loadbalancer.models.ResponseMessage;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SpringBootTest
public class LoadBalancerIntegrationTest extends ServerFixtures {
    private int port = 9080;

    private final RestTemplate restTemplate = new RestTemplate();

    @Test
    public void testLoadBalancer() {
        // Set algorithm
        ResponseEntity<String> algorithmResponse = restTemplate.getForEntity("http://localhost:" + port + "/liftlab/lb/algorithm/roundrobin", String.class);
        assertEquals(200, algorithmResponse.getStatusCode().value());

        // Remove all servers
        ResponseEntity<String> allServers = restTemplate.getForEntity("http://localhost:" + port + "/liftlab/servers/", String.class);
        new ObjectMapper().convertValue(allServers.getBody(), PageImpl.class).stream()
                .forEach( serverUrl -> restTemplate.delete("http://localhost:" + port + "/liftlab/servers/?url=" + serverUrl));

        // Add server 1
        ResponseEntity<String> server1Response = restTemplate.postForEntity("http://localhost:" + port + "/liftlab/servers/?url=" + backendServer1.getUrl(), null, String.class);
        assertEquals(200, server1Response.getStatusCode().value());

        // Add server 2
        ResponseEntity<ResponseMessage> server2Response = restTemplate.postForEntity(
                "http://localhost:" + port + "/liftlab/servers/?url=" + backendServer2.getUrl(),
                null, ResponseMessage.class);
        assertEquals(200, server2Response.getStatusCode().value());

        // Add server 3
        ResponseEntity<String> server3Response = restTemplate.postForEntity("http://localhost:" + port + "/liftlab/servers/?url=" + backendServer3.getUrl(), null, String.class);
        assertEquals(200, server3Response.getStatusCode().value());

        // Check routing
        ResponseEntity<String> server1Route = restTemplate.getForEntity("http://localhost:" + port + "/liftlab/lb/test", String.class);
        assertEquals(200, server1Route.getStatusCode().value());
        assertTrue(backendServer1.getUrl(), Objects.requireNonNull(server1Route.getBody()).contains(backendServer1.getUrl()));

        ResponseEntity<String> server2Route = restTemplate.getForEntity("http://localhost:" + port + "/liftlab/lb/test", String.class);
        assertEquals(200, server2Route.getStatusCode().value());
        assertTrue(Objects.requireNonNull(server2Route.getBody()).contains(backendServer2.getUrl()));

        ResponseEntity<String> server3Route = restTemplate.getForEntity("http://localhost:" + port + "/liftlab/lb/test", String.class);
        assertEquals(200, server3Route.getStatusCode().value());
        assertTrue(Objects.requireNonNull(server3Route.getBody()).contains(backendServer3.getUrl()));
    }
}
