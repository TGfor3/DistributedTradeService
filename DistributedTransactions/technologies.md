# Technologies

**RabbitMQ** for Distributed Queue Service to handle incoming Client requests
: Chosen for its ease of setup, and flexible Message Passing. RabbitMQ will be the core of how we receive Buy and Sell requests from our clients and distribute them to the worker nodes to be executed. The other option we considered was Kafka, but we decided that the ease of setup of RabbitMQ was very important when trying to integrate a bunch of new to us technologies

**MongoDB** for Portfolio and Brokerage
: Chosen for its built in Sharding and recovery, mongoDB will store all of the client information as well as the Stock Inventory. Transactions will be made by sending Updates that require bank balance and stock inventory to remain non-negative and to return an update error if those requirements are not met.

**Zookeeper** for our orchestrator algorithm to handle concurrency and fault tolerance
: In order to ensure Fault tolerance and scalability. We have chosen Zookeeper with a backup leader node to facilitate quick assignment of work and ease of recovery. Items sent to worker nodes from the leader node will be moved from the incoming transaction queue, to the In Process queue.

**Java** Our main coding language, chosen for the extreme levels of familiarity we have with it
: While the project can be done in Python, we feel that our familiarity with Java will help us complete this project to the best of our abilities
