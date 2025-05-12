package com.liftlab.loadbalancer.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BackendServer {
    private final String url;
    private boolean healthy = true;

    public BackendServer(String url) {
        this.url = url;
    }
}
