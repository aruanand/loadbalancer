package com.liftlab.loadbalancer.controllers;

import com.liftlab.loadbalancer.models.ListResponseEntity;
import com.liftlab.loadbalancer.models.ResponseMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.liftlab.loadbalancer.services.LoadBalancerService;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/servers")
public class ServerRegistryController {
    private final LoadBalancerService service;

    private static final Logger LOGGER = LogManager.getLogger(ServerRegistryController.class);

    @Autowired
    ServerRegistryController(LoadBalancerService loadBalancerService){
        service = loadBalancerService;
    }

    @PostMapping("/")
    public ResponseEntity<ResponseMessage> addServer(@RequestParam String url) {
        LOGGER.info("Received request to create new server with URL {}", url);

        return service.registerServer(url);
    }

    @DeleteMapping("/")
    public ResponseEntity<ResponseMessage> removeServer(@RequestParam String url) {
        LOGGER.info("Received request to delete server with URL {}", url);

        return service.removeServer(url);
    }

    @PutMapping("/algorithm/{algorithm}")
    public ResponseEntity<ResponseMessage> changeAlgorithm(@PathVariable String algorithm) {
        LOGGER.info("Received request to set default algorithm to {}", algorithm);

        return service.changeAlgorithm(algorithm);
    }

    @PutMapping("/health")
    public ResponseEntity<ResponseMessage> changeHealth(@RequestParam String url,
                                               @RequestParam boolean healthy) {
        LOGGER.info("Received request to change server health status of {} to {}", url, healthy);

        return service.changeHealth(url, healthy);
    }

    @GetMapping("/")
    public ListResponseEntity<PageImpl<String>> listServers(
            @RequestParam(required = false, defaultValue = "0") Integer pageNo,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize
    ) {
        LOGGER.info("Received request to list all registered servers");

        if(pageSize == null)
            pageSize = 10;
        return service.getActiveServerUrls(Pageable.ofSize(pageSize).withPage(pageNo));
    }
}
