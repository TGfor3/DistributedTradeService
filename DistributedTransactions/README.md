# Trade transaction processing

**Team Members:** [Gabriel Aspir](mailto:gaspir@mail.yu.edu), [Isaac Gutt](mailto:igutt@mail.yu.edu), [Mordechai Sheinson](mailto:msheinso@mail.yu.edu)

## Project Plan

- [Scope and Use Cases](scope.md)
- [Distributed System Challenges](challenges.md)
- [Workflow Diagrams (BPMN)](workflow.md)
- [Software Architecture Diagrams (C4)](architecture.md)
- [Tools & Technologies](technologies.md)
- [Installation Instructions](installation.md)
- [API Documentation](api.md)


## Overview

This project aims to transform a trading application into a distributed system. This trade server facilitates various stock market operations, including retrieving stock prices, registering clients, managing portfolios, and processing buy/sell requests. The goal is to distribute these functionalities across a network to enhance scalability, reliability, and performance.

Our Group serves as the core of the Trade Processing Group. Our job is to receive and execute trades from clients in a distributed manner while maintaining transactional integrity and consistency.

We interface with Trade Processing Group 3 to receive up to date stock prices to help us calculate trade values, and in return supply them with trade data to assist in their system monitoring efforts.

We interface with Trade Processing Group 1 to receive new client transactions, and we povide them with a queue of completed transactions they can use to update their client tables
