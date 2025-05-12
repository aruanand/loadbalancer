package com.liftlab.loadbalancer.helpers;


import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfig {

    @Bean
    public RestTemplate restTemplate() {
        // Connection Pool Manager
        PoolingHttpClientConnectionManager poolingConnManager = new PoolingHttpClientConnectionManager();
        poolingConnManager.setMaxTotal(50); // total open connections
        poolingConnManager.setDefaultMaxPerRoute(10); // per route limit

        // Create the HTTP client
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(poolingConnManager)
                .evictExpiredConnections()
                .evictIdleConnections(Timeout.ofSeconds(30))
                .build();

        // Configure timeout settings
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        requestFactory.setConnectTimeout(5000);     // 5 sec
        requestFactory.setReadTimeout(5000);        // 5 sec
        requestFactory.setConnectionRequestTimeout(5000);

        return new RestTemplate(requestFactory);
    }
}
