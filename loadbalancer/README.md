# Load Balancer

## Description

The goal is to design and implement a simple yet functional **load balancer**. 
This load balancer should be able to distribute HTTP requests among a set of backend servers using **round-robin scheduling** by default. 
It should also support **algorithm switching**, dynamic backend registration/removal, and handle **concurrent requests efficiently**.

## Features

1. Listens for incoming HTTP requests on a configurable port.
2. Supports multiple load balancing algorithms:
    - Round-Robin (default)
    - Random Selection
3. Forwards requests to selected backend server and returns the response.
4. Efficiently handles multiple concurrent requests.
5. Supports dynamic registration/removal of backend servers.
6. Handles failed backend requests gracefully.
7. Built-in support for logging/debugging.
8. Modular design allows easy scaling and extension.

## Optional Features

1. Health check mechanism to monitor backend availability.
2. Automatic exclusion of unhealthy servers and re-inclusion upon recovery.
3. Extendable architecture to add other algorithms (e.g., Least Connections).

## Setup Instructions

### Prerequisites

- Java 17+
- Maven 3.6+
- Spring Boot 3.x

### How to Run

1. Clone the repository
2. Configure backend server list in `application.properties`
3. Run the application:
   mvn spring-boot:run
4. Send HTTP requests to:
   http://localhost:9080/liftlab

## High-Level Architecture

- Load Balancer Controller → Request Dispatcher → Algorithm Handler → Backend Forwarder → Response Relay
- Uses a registry to store live backend servers and their metadata.

## Low-Level Call Flow

1. Request received at Load Balancer Controller
2. Algorithm decides backend (e.g., next server in round-robin)
3. Request is forwarded to chosen backend
4. Client connection pooling
5. Backend response is returned to client
6. Errors handled and logged appropriately

## Extensibility

- Easily plug in new algorithms via `LoadBalancingAlgorithm` interface
- Add health checks using `@Scheduled` tasks
- Backend registry can be extended to use Redis, PostgreSQL, or service discovery tools (e.g., Eureka)