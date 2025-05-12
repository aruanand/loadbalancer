
package com.liftlab.loadbalancer;

import com.liftlab.loadbalancer.algorithms.LoadBalancingAlgorithm;
import com.liftlab.loadbalancer.controllers.LoadBalancerController;
import com.liftlab.loadbalancer.fixtures.ServerFixtures;
import com.liftlab.loadbalancer.models.ResponseMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.liftlab.loadbalancer.services.LoadBalancerService;
import org.springframework.web.servlet.NoHandlerFoundException;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LoadBalancerControllerTest extends ServerFixtures {


    @Mock
    private LoadBalancerService service;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private LoadBalancerController controller;

    @Test
    void testSetAlgorithm() throws NoHandlerFoundException {
        when(service.forwardRequest(any(), any(LoadBalancingAlgorithm.class)))
                .thenReturn(ResponseEntity.ok(new ResponseMessage(backendServer1.getUrl())));

        ResponseEntity<ResponseMessage> response = controller.setStrategy("roundrobin");
        assertEquals(200, response.getStatusCodeValue());

        response = controller.setStrategy("random");
        assertEquals(200, response.getStatusCodeValue());

        response = controller.setStrategy("unknown");
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void testRouteRequest() throws NoHandlerFoundException {
        when(service.forwardRequest(any(), any(LoadBalancingAlgorithm.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body((new ResponseMessage("Request forwarding failed: "))));
        ResponseEntity<ResponseMessage> response = controller.route(request);
        assertEquals(502, response.getStatusCode().value());

        when(service.forwardRequest(any(), any(LoadBalancingAlgorithm.class)))
                .thenReturn(new ResponseEntity<>(new ResponseMessage(backendServerList.getFirst().getUrl()),
                        HttpStatus.OK));

        response = controller.route(request);
        assertEquals(200, response.getStatusCode().value());


    }

    @Test
    void testHealthSwitchRequest() throws NoHandlerFoundException {
        when(service.getActiveServers()).thenReturn(backendServerList);
        when(service.forwardRequest(any(), any(LoadBalancingAlgorithm.class)))
                .thenReturn(ResponseEntity.ok(new ResponseMessage(backendServer1.getUrl())));

        ResponseEntity<ResponseMessage> response = controller.route(request);
        assertEquals(200, response.getStatusCode().value());

        when(service.forwardRequest(any(), any(LoadBalancingAlgorithm.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body(new ResponseMessage("Request forwarding failed: ")));
        response = controller.route(request);
        assertEquals(502, response.getStatusCode().value());
    }
}
