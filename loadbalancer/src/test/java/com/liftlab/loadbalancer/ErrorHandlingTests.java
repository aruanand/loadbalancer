package com.liftlab.loadbalancer;

import com.liftlab.loadbalancer.controllers.GlobalExceptionHandler;
import com.liftlab.loadbalancer.models.ResponseMessage;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Objects;

import static org.junit.Assert.*;

@SpringBootTest
public class ErrorHandlingTests {
    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    public void testHandleAllExceptions() {
        Exception ex = new Exception("Test exception");
        ResponseEntity<ResponseMessage> response = exceptionHandler.handleAllExceptions(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).getMessage().contains("Internal Server Error"));
    }

    @Test
    public void testHandleNoHandlerFoundException() {
        NoHandlerFoundException ex = new NoHandlerFoundException("GET", "/unknown", null);
        ResponseEntity<ResponseMessage> response = exceptionHandler.handleNoHandlerFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Route not found:" + " /unknown", Objects.requireNonNull(response.getBody()).getMessage());
    }
}
