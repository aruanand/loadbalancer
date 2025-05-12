package com.liftlab.loadbalancer.controllers;

import com.liftlab.loadbalancer.algorithms.LoadBalancingAlgorithm;
import com.liftlab.loadbalancer.algorithms.RoundRobinAlgorithm;
import com.liftlab.loadbalancer.models.ResponseMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.liftlab.loadbalancer.services.LoadBalancerService;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/lb")
public class LoadBalancerController {
    private final Map<String, LoadBalancingAlgorithm> strategies;
    private LoadBalancingAlgorithm strategy;
    private final LoadBalancerService service;

    private static final Logger LOGGER = LogManager.getLogger(LoadBalancerController.class);

    @Autowired
    public LoadBalancerController(LoadBalancerService service) {
        this.service = service;
        this.strategies = LoadBalancingAlgorithm.getStrategyList().stream()
                .collect(Collectors.toMap(s -> s.getAlgorithmName().toLowerCase(), s -> s));
        this.strategy = strategies.get(new RoundRobinAlgorithm().getAlgorithmName()); // default
    }

    @GetMapping(value = "/algorithm/{algo}", produces = "application/json")
    public ResponseEntity<ResponseMessage> setStrategy(@PathVariable String algo) {
        LOGGER.info("Received request to set load balancer strategy to: {}", algo);
        LoadBalancingAlgorithm selected = strategies.get(algo.toLowerCase());
        if (selected == null)
            return ResponseEntity.badRequest().body(new ResponseMessage("Unknown algorithm"));
        this.strategy = selected;
        return ResponseEntity.ok(new ResponseMessage("Strategy set to " + algo));
    }

    @GetMapping("/**")
    public ResponseEntity<ResponseMessage> route(HttpServletRequest request) throws NoHandlerFoundException {
        LOGGER.info("Received request to route request to server");
        return service.forwardRequest(request, strategy);
    }
}