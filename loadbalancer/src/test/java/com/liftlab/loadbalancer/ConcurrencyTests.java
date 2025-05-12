package com.liftlab.loadbalancer;

import com.liftlab.loadbalancer.algorithms.LoadBalancingAlgorithm;
import com.liftlab.loadbalancer.fixtures.ServerFixtures;
import com.liftlab.loadbalancer.controllers.LoadBalancerController;
import com.liftlab.loadbalancer.models.ResponseMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.liftlab.loadbalancer.services.LoadBalancerService;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.concurrent.CountDownLatch;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ConcurrencyTests extends ServerFixtures {


    @Mock
    private LoadBalancerService service;

    @InjectMocks
    private LoadBalancerController loadBalancerController;

    @Test
    void testConcurrentRequestHandling() throws InterruptedException, NoHandlerFoundException {
        // Setup mock responses
        when(service.forwardRequest(any(), any(LoadBalancingAlgorithm.class)))
                .thenReturn(new ResponseEntity<>(new ResponseMessage(backendServerList.getFirst().getUrl()),
                        HttpStatus.OK));
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/test");

        // Simulate concurrent requests
        CountDownLatch latch = new CountDownLatch(2);
        Runnable requestTask = () -> {
            try {
                ResponseEntity<ResponseMessage> response = loadBalancerController.route(request);
                assertEquals(200, response.getStatusCode().value());
            } catch (Exception e) {
                fail("Request failed: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        };

        // Start concurrent tasks
        new Thread(requestTask).start();
        new Thread(requestTask).start();

        latch.await();  // Ensure both threads finish
        verify(service, times(2)).forwardRequest(any(), any(LoadBalancingAlgorithm.class));  // Assert forwardRequest is called twice
    }
}
