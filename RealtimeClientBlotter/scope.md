# Project Description
The ClientBlotterService is built to provide clients with a realtime view of their current holdings, including shares, share prices, and market values. The system is distributed, fault tolerant, and resilient, enabling constant, reliable access, even in case of failure. The system is also simple and easy to use

# Project Objectives
- Provide a constantly updating view of a client's holdings
    - 200ms latency
        - (Phase 2) 50ms latency
- Provide a distributed system that continues to function in case of failure
- Filter and route Kafka messages based on client ID
- Provide a simple, intuitive UI and UX

# Features
- **Realtime**: Updates are streamed to clients in real time
- **Distributed**: Each service is made up of multiple identical machines, allowing for availability, scalability, fault tolerance, and reliability
- **Market Value**: Clients receive their current market share for each of their holdings
- **Automatic Reconnect**: In case the client is disconnected, automatic reconnect/reroute will be attempted automatically
- **Minimal Client Involvement**: Client input should be minimally necessary
- **Multi-User, Single Account**: Multiple users can access the same account and all will receive the same realtime view of the data

## Use Cases

- **Live View**:
    - The client simply supplies their clientId and updates will be shown in realtime, including new trades and price changes
        - The client begins by navigating to the URL of the HaProxy instance. They will then be routed to an API Gateway instance, which will return a login page
        - The client will supply their clientId, which will be returned to the gateway
        - The gateway will use the supplied clientId to determine which BlotterService machine to route the client to.
            - If another user is accessing the same account on another BlotterService machine, the new user will be serviced by the same  machine.
            - Otherwise, the user will be routed to a random BlotterService node
        - The webpage returned to the client will initiate a connection to the supplied BlotterService node, opening a channel for realtime updates.
            - This connection will remain open until the client closes the window or the BlotterService node fails (see below)
- 

## Failure Scenarios
- **Gateway Failure**
    If an API Gateway instance fails, HaProxy will simply route new clients to other API Gateway instances
- **BlotterService Failure**
    In case of a BlotterService instance failure, all clients will be disconnected from the machine servicing them and an automatic reconnect will be attempted. The API Gateway will ensure that no clients are routed to the disabled machine(s)
- **ClientDataRoutingService Failure**
    The Kafka topic the ClientDataRoutingService consumes from is partitioned, to enable division of the topic between machines. Accordingly, a ClientDataRoutingService instance is always kept ready in a "cold" state, in case of failure. In such a case, the "cold" machine will take over the partition of the ClientDataRoutingService node that went down, ensuring the continued, uninterrupted functioning of the system.
- **Client Disconnect**
    If the client is disconnected from the BlotterService node servicing it, reconnection will be attempted from the client and server side automatically