# Documentation
This document provides step by step instructions for using the ClientBlotterService
## Prerequisites
- Docker
## Local Quickstart
- Begin by pulling the code found [here](../services/T1Deploy/)
- Uncomment the top portion of the [.env file](../services/T1Deploy/.env) (marked 'local') and comment out the bottom section (marked 'distributed')
- Ensure that the top three services in the [docker-compose.yml](../services/T1Deploy/docker-compose.yml) are uncommented
    - Since the BlotterSerice and CDRS depend on Kafka, ensure that the kafka element in the 'depends-on' list for all instances of those services is uncommented
- Update the [HaProxy config](../services/T1Deploy/HAProxy/haproxy.cfg) to route requests appropriately
    - The current version assumes that the HaProxy instance is running as part of the same docker-compose as the Api Gateway instances.
- Before running, rebuild the two Java services by running `mvn clean package` in the appropriate root directory [here](../services/T1Deploy/ClientDataRoutingService/) and [here](../services/T1Deploy/HazelCast/)
- Run `docker compose up --build`

## Distributed Quickstart
- Begin by pulling the code found [here](../services/T1Deploy/)
- Ensure that the top portion of the [.env file](../services/T1Deploy/.env) (marked 'local') is commented out and the bottom section (marked 'distributed') is uncommented
    - In the [.env file](../services/T1Deploy/.env) ensure that:
        - MONGO_IP is set to the IP of the machine that will be running the [infrastructure code](../services/InfrastructureDeploy/)
        - MONGO_PORT is set to the port on the infrastructure machine Mongo is listening on
            - 27017, by default
        - KAFKA_URL is set to the IP address of the machine running the [infrastructure code](../services/InfrastructureDeploy/) and the port on which Kafka is listening
            - 9092 by default
        - CLIENT_ACCESS_HOST is set to the IP address of a machine running the ClientBlotterService containers (i.e. _this machine_)
- Ensure that the top three services in the [docker-compose.yml](../services/T1Deploy/docker-compose.yml) are commented out
    - Ensure that the kafka element in the 'depends-on' list for all instances of those services is commented out
- Update the [HaProxy config](../services/T1Deploy/HAProxy/haproxy.cfg) to route requests appropriately
    - The current version assumes that the HaProxy instance is running as part of the same docker-compose as the Api Gateway instances.
    - If that is not the case, "gateway1" and "gateway2" should be replaced with the IP address of the machine running the Api Gateway instances. The ports should also align with the ports those Api Gateway instances are listening on
- Before running, rebuild the two Java services by running `mvn clean package` in the appropriate root directory [here](../services/T1Deploy/ClientDataRoutingService/) and [here](../services/T1Deploy/HazelCast/)
- Ensure that the infrastructure containers are running (see [here](../services/InfrastructureDeploy/))
- Run `docker compose up --build` to start the ClientBlotter


## Kafka Topic and Schema
The CDRS consumes from the MarketValueTopic and messages are expected in the following schema:
```
{
    ticker: string,
    quantity: int,
    price: double,
    market_value: double,
    price_last_updated: double,
    holding_last_updated: double
}
```