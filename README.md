# Distributed Trade Service

The primary objective of this project is to enhance a high-performance, single-node, multi-threaded trading application by extending its capabilities across several nodes. This distributed trading service encompasses a range of stock market functions such as registering client accounts, processing trade requests, providing real-time updates on stock prices, and calculating the market value of client portfolios. The aim is to spread these operations across a network to improve scalability, reliability, and overall performance. The system will also be designed to be observable, enabling real-time monitoring and analysis of its operations, which is crucial for identifying and addressing issues promptly and optimizing system performance.

## Project Groups

1. **Client registration, positions and market value** - _highly available and efficient distributed services supporting client registration, and the display of real-time positions and market value._
   - Team Members: [Nir Solooki](mailto:nsolooki@mail.yu.edu>), [Tuvia Goldstein](mailto:tjgoldst@mail.yu.edu), [Asher Kirshtein](mailto:ackirsht@mail.yu.edu>)
   - [Overview](./RealtimeClientBlotter/README.md)
   - [Scope and Use Cases](./RealtimeClientBlotter/scope.md)
   - [Distributed System Challenges](./RealtimeClientBlotter/challenges.md)
   - [Workflow Diagrams (BPMN)](./RealtimeClientBlotter/workflow.md)
   - [Software Architecture Diagrams (C4)](./RealtimeClientBlotter/architecture.md)
   - [Tools & Technologies](./RealtimeClientBlotter/technologies.md)

2. **Trade transaction processing** - _a scalable and highly efficient distributed system for processing buy and sell transaction requests._
   - Team Members: [Gabriel Aspir](mailto:gaspir@mail.yu.edu), [Isaac Gutt](mailto:igutt@mail.yu.edu), [Mordechai Sheinson](mailto:msheinso@mail.yu.edu)
   - [Overview](./DistributedTransactions/README.md)
   - [Scope and Use Cases](./DistributedTransactions/scope.md)
   - [Distributed System Challenges](./DistributedTransactions/challenges.md)
   - [Workflow Diagrams (BPMN)](./DistributedTransactions/workflow.md)
   - [Software Architecture Diagrams (C4)](./DistributedTransactions/architecture.md)
   - [Tools & Technologies](./DistributedTransactions/technologies.md)
  
3. **Real-time prices + system observability** - _provide real-time streaming prices; implement a framwork for observability and monitoring of the system_
   - Team Members: [Eli Levy](mailto:emlevy1@mail.yu.edu), [Oze Botach](mailto:obotach@mail.yu.edu), [Shmuel Newmark](mailto:snewmark@mail.yu.edu)
   - [Overview](./MetricsAndMVC/README.md)
   - [Scope and Use Cases](./MetricsAndMVC/scope.md)
   - [Distributed System Challenges](./MetricsAndMVC/challenges.md)
   - [Workflow Diagrams (BPMN)](./MetricsAndMVC/workflow.md)
   - [Software Architecture Diagrams (C4)](./MetricsAndMVC/architecture.md)
   - [Tools & Technologies](./MetricsAndMVC/technologies.md)
