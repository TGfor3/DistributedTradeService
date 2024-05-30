# Distributed System Challenges

## Use Cases and Reasons for Going Distributed

- Scalability: Handling spikes in trading activity during market openings or major news events requires a distributed architecture to scale resources efficiently.
- Fault Tolerance: Distributing critical components across multiple nodes ensures uninterrupted trading operations and protects against single points of failure.
- High availablity: Ensuring continuous access to the trade platform even in the face of failures requires distributing components across multiple servers.
- Load Balancing: Distributing incoming trade requests evenly across multiple servers to prevent overload to a single server requires load balancing algorithms.

## Going Distributed addresses these issues:

1. **Scalability**
   - By distributing the trade service across multiple servers, we can horizontally scale the system to handle increasing numbers of concurrent users and trading activity.
   
2. **Fault Tolerance**
   - Distributing the trade service improves fault tolerance by introducing redundancy and isolating failures.
   - If one node fails, other nodes can continue to process trades seamlessly.

3. **High Availability**
   - Distributing the services across multiple nodes enhances availability by providing failover options in case of outages.
   - In case of a server failure, requests are automatically redirected to a backup server, ensuring uninterrupted service.

4. **Load Balancing**
   - By allowing equal distribution of work across all nodes based on the nodes' workload and resource utilization, we can prevent overload on individual nodes.

## Challenges that result from going Distributed:

1. **Network Communication Overhead**
   - Distributing the trade service introduces increased network communication between nodes, leading to potential latency issues and increased bandwidth consumption.
  
2. **Consistency and Synchronization**
   - Ensuring consistency and synchronization of trade data across distributed nodes can be extremely challenging, leading to issues such as data inconsistencies and race conditions.

3. **Load Balancing**
   - Load balancing the workload across distributed nodes brings complexity into the system.

## Proposed solutions to Distributed Challenges

1. **Network Communication Overhead**
   - Implement efficient communication protocols, such as TCP or HTTP.
   - Minimize unnecessary data transfer.
   - Utilize open source tools, such as Apache Kafka, for getting messages from point A to point B.
  
2. **Consistency and Synchronization**
   - Implement transactional mechanisms like the SAGA pattern for atomicity and isolation.
   - Utilize open source tools, such as MongoDB, for idempotency.
   - Employ data replication and synchronization techniques, such as a read-only copy DB, to maintain consistency across Databases.

3. **Load Balancing**
   - Employ dynamic load balancing algorithms to distribute workload evenly.
   - Utilize open source tools, such as Apache Kafka, and ZooKeeper, to efficiently manage load.

## Phase Two Enhancements
1. **Security Concerns**
   - Distributing the trade service increases the attack surface, exposing it to various security threats such as unauthorized access, data breaches, and distributed denial-of-service (DDoS) attacks.

2. **System Integrity**
   - Ensuring the integrity of the distributed trade system involves preventing unauthorized access, maintaining data consistency,  and detecting and mitigating potential security breaches.
