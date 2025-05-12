package com.liftlab.loadbalancer;

public class ApplicationConstants {
    // made ApplicationConstant as singleton to avoid anyone changing the values
    private ApplicationConstants(){}

    public static final String ROUND_ROBIN_ALGORITHM_NAME = "roundrobin";
    public static final String RANDOM_ALGORITHM_NAME = "random";

}
