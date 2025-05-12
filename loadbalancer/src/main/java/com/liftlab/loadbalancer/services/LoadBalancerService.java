package com.liftlab.loadbalancer.services;

import com.liftlab.loadbalancer.algorithms.LoadBalancingAlgorithm;
import com.liftlab.loadbalancer.models.PagedResponse;
import com.liftlab.loadbalancer.models.ResponseMessage;
import jakarta.servlet.http.HttpServletRequest;
import com.liftlab.loadbalancer.models.BackendServer;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

import static com.liftlab.loadbalancer.ApplicationConstants.ROUND_ROBIN_ALGORITHM_NAME;

@Service
public class LoadBalancerService {
    private final ServerRegistryService serverRegistryService;
    private final RestTemplate restTemplate;

    private static final Logger LOGGER = LogManager.getLogger(LoadBalancerService.class);

    @Autowired
    LoadBalancerService(ServerRegistryService registry, RestTemplate restTemplate){
        this.serverRegistryService = registry;
        this.restTemplate = restTemplate;
    }

    private static LoadBalancingAlgorithm algorithm = LoadBalancingAlgorithm.getAlgorithm(ROUND_ROBIN_ALGORITHM_NAME);

    public ResponseEntity<ResponseMessage> registerServer(String url) {
        if(StringUtils.isBlank(url))
            return new ResponseEntity<>(new ResponseMessage("URL is required to add new server"),
                    HttpStatus.NOT_ACCEPTABLE);

        LOGGER.info("Adding new server");
        if(this.serverRegistryService.getAvailableServers().stream()
                .anyMatch( server -> server.getUrl().equalsIgnoreCase(url)))
            return new ResponseEntity<>(new ResponseMessage("Failed to add server, as it is already registered!"),
                    HttpStatus.NOT_ACCEPTABLE);
        this.serverRegistryService.registerServer(new BackendServer(url));
        return new ResponseEntity<>(new ResponseMessage("Successfully added server with URL : " + url),
                HttpStatus.OK);
    }

    public ResponseEntity<ResponseMessage> removeServer(String url) {
        LOGGER.info("Removing server");
        if(StringUtils.isBlank(url))
            return new ResponseEntity<>(new ResponseMessage("URL is required to remove server"),
                    HttpStatus.NOT_ACCEPTABLE);

        List<BackendServer> activeServers = this.getActiveServers();
        if(activeServers.isEmpty())
            return new ResponseEntity<>(new ResponseMessage("No server registered yet"),
                    HttpStatus.NOT_ACCEPTABLE);

        List<BackendServer> toBeDeleted =
                activeServers.stream().filter(server -> server.getUrl().equalsIgnoreCase(url)).toList();
        if(toBeDeleted.isEmpty())
            return new ResponseEntity<>(new ResponseMessage("No server registered with the URL: " + url),
                    HttpStatus.NOT_ACCEPTABLE);
        else if (toBeDeleted.size() > 1) {
            return new ResponseEntity<>(new ResponseMessage("Multiple servers found with URL: " + url),
                    HttpStatus.NOT_ACCEPTABLE);
        }

        if(removeActiveServer(toBeDeleted.getFirst()))
            return new ResponseEntity<>(new ResponseMessage("Successfully added server with URL : " + url),
                    HttpStatus.OK);
        return new ResponseEntity<>(new ResponseMessage("Failed to remove server"),
                HttpStatus.NOT_ACCEPTABLE);
    }


    public ResponseEntity<ResponseMessage> changeAlgorithm(String algo) {
        if(StringUtils.isBlank(algo))
            return new ResponseEntity<>(new ResponseMessage("Algorithm name is required"),
                    HttpStatus.NOT_ACCEPTABLE);

        if(algorithm.getAlgorithmName().equalsIgnoreCase(algo))
            return new ResponseEntity<>(new ResponseMessage("Algorithm name already selected"),
                    HttpStatus.NOT_ACCEPTABLE);

        algorithm = LoadBalancingAlgorithm.getAlgorithm(algo);
        LOGGER.info("Changed default algorithm to {}", algo);

        return new ResponseEntity<>(new ResponseMessage("Successfully switched to : " + algo),
                HttpStatus.OK);
    }

    public ResponseEntity<ResponseMessage> forwardRequest(HttpServletRequest clientRequest, LoadBalancingAlgorithm customAlgorithm)
            throws NoHandlerFoundException {
        List<BackendServer> healthy = serverRegistryService.getAvailableServers().stream().filter(BackendServer::isHealthy).toList();
        if (healthy.isEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage("No healthy servers"));

        BackendServer selected = customAlgorithm.selectServer(serverRegistryService);
        try {
            LOGGER.info("Forwarding request to server: {}", selected.getUrl());

            return poolAndForwardRequest(clientRequest, selected.getUrl());
        } catch (NoHandlerFoundException e){
            LOGGER.error("Couldn't complete request");
            throw e;
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new ResponseMessage("No servers available"));
        }
    }

    public PagedResponse<String> getActiveServerUrls(Pageable page) {
        LOGGER.info("Fetching all active servers urls");

        List<String> activeServers = serverRegistryService.getAvailableServers().stream().filter(BackendServer::isHealthy).map(BackendServer::getUrl).toList();
        if(activeServers.isEmpty())
            throw new NoSuchElementException("No active servers found");

        PageImpl<String> serversPage = new PageImpl<>(activeServers, page, activeServers.size());
        return new PagedResponse<>().convertToPagedResponse(serversPage);
    }
    public List<BackendServer> getActiveServers() {
        LOGGER.info("Fetching all active servers");

        return serverRegistryService.getAvailableServers().stream().filter(BackendServer::isHealthy).toList();
    }

    public boolean removeActiveServer(BackendServer server) {
        LOGGER.info("Removing active server with URL: {}", server.getUrl());

        serverRegistryService.removeServer(server);
        return true;
    }

    public ResponseEntity<ResponseMessage> poolAndForwardRequest(HttpServletRequest incomingRequest, String targetUrl) throws NoHandlerFoundException {
        HttpMethod method = HttpMethod.valueOf(incomingRequest.getMethod());
        HttpHeaders headers = extractHeaders(incomingRequest);
        try {
            if (method == null) {
                return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                        .body(new ResponseMessage("Unsupported HTTP method"));
            }

            byte[] body = incomingRequest.getInputStream().readAllBytes();

            HttpEntity<byte[]> entity = new HttpEntity<>(body, headers);
            LOGGER.info("Forwarding request to server");

            return ResponseEntity.ok().body(new ResponseMessage(targetUrl));
            // TODO: uncomment following line to send actual requests to registered endpoints
//            return restTemplate.exchange(targetUrl, method, entity, String.class);

        } catch (RestClientException e){
            throw new NoHandlerFoundException(method.name(), targetUrl, headers);
        }
        catch (IOException e) {
            // Log error and return 502 Bad Gateway
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(new ResponseMessage("Request forwarding failed: " + e.getMessage()));
        }
    }

    private HttpHeaders extractHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames != null && headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> values = request.getHeaders(headerName);
            while (values.hasMoreElements()) {
                headers.add(headerName, values.nextElement());
            }
        }

        return headers;
    }

    public ResponseEntity<ResponseMessage> changeHealth(String url, boolean healthy) {
        if(serverRegistryService.changeHealth(url, healthy))
            return new ResponseEntity<>(new ResponseMessage("Successfully changed health status of " + url + " to " + healthy),
                    HttpStatus.OK);
        return new ResponseEntity<>(new ResponseMessage("No active server found for " + url),
                HttpStatus.NOT_ACCEPTABLE);
    }
}
