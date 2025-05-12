package com.liftlab.loadbalancer.fixtures;

import com.liftlab.loadbalancer.models.BackendServer;

import java.util.List;

public class ServerFixtures {
    protected BackendServer backendServer1 = new BackendServer("https://l1.liftlab.com");
    protected BackendServer backendServer2 = new BackendServer("https://l2.liftlab.com");
    protected BackendServer backendServer3 = new BackendServer("https://l3.liftlab.com");
    protected BackendServer backendServer4 = new BackendServer("https://l4.liftlab.com");
    protected BackendServer backendServer5 = new BackendServer("https://l5.liftlab.com");

    protected List<BackendServer> backendServerList = List.of(backendServer1,
            backendServer2, backendServer3);
}
