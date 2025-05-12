package com.liftlab.loadbalancer.controllers;

import com.liftlab.loadbalancer.models.ResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {
    // Handle any kind of RuntimeException (or specific exception type if required)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseMessage> handleAllExceptions(Exception ex) {
        // Log the exception here (could be to a file or logging system)
        return new ResponseEntity<>(
                new ResponseMessage("Internal Server Error: " + ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Handle NoHandlerFoundException (for undefined routes)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ResponseMessage> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        return new ResponseEntity<>(new ResponseMessage("Route not found: " + ex.getRequestURL()),
                HttpStatus.NOT_FOUND);
    }
}

