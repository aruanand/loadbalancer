package com.liftlab.loadbalancer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liftlab.loadbalancer.fixtures.ServerFixtures;
import com.liftlab.loadbalancer.models.PagedResponse;
import com.liftlab.loadbalancer.models.ResponseMessage;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SpringBootTest
public class LoadBalancerIntegrationTest extends ServerFixtures {
    private int port = 9080;

    private final RestTemplate restTemplate = new RestTemplate();

    @Test
    public void testLoadBalancer() throws JsonProcessingException {
        // Set algorithm
        ResponseEntity<String> algorithmResponse = restTemplate.getForEntity("http://localhost:" + port + "/liftlab/lb/algorithm/roundrobin", String.class);
        assertEquals(200, algorithmResponse.getStatusCode().value());

        // Remove all servers
        ResponseEntity<String> allServers;
        try {
            allServers = restTemplate.getForEntity("http://localhost:" + port + "/liftlab/servers/", String.class);
        } catch (HttpClientErrorException e){
            allServers = new ResponseEntity<>("No data", HttpStatus.NOT_FOUND);
        }
        if(allServers.getStatusCode().value() == 200) {
            JavaType type = new ObjectMapper().getTypeFactory()
                    .constructParametricType(PagedResponse.class, String.class);

            PagedResponse<String> pagedResponse = Objects.requireNonNull(new ObjectMapper().readValue(allServers.getBody(), type));
            pagedResponse.getContent().forEach(serverUrl -> restTemplate.delete("http://localhost:" + port + "/liftlab/servers/?url=" + serverUrl));
        }

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
