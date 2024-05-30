## Technologies

Kafka for Event Intake and Processing:
Chosen for its high throughput, scalability, and fault tolerance, Kafka will handle the intake of events and prices from various clusters. Events sent to Kafka are processed by our event processing system, which waits for all required keys and applies relevant logic based on the event type. The compiled event is then sent to Postgres for storage. Kafka was selected over other messaging systems for its robust performance in handling large volumes of data with low latency.

**Postgres for Cold Storage:**
Chosen for its reliability and strong support for transactional operations, Postgres will store the processed events and historical data. It provides robust data integrity and ensures that our data is easily accessible for analysis and auditing. Postgres was chosen for its strong ACID compliance and support for complex queries, making it ideal for our needs.

**Grafana for Observability:**
Grafana will be used to create dashboards that display data from all teams/clusters in the trade server. These dashboards allow users to see their logs and data in graphical formats. With color-coded indicators (green, orange, red), users can quickly assess the status of their systems, identifying areas that are operating smoothly or require attention. Grafana was chosen for its flexibility and powerful visualization capabilities.

**Docker for Container Management:**
Docker will be used to manage the deployment and operation of our applications, ensuring that each component runs in a consistent environment. This approach simplifies the deployment process, improves scalability, and allows for easier maintenance and updates. Docker was chosen for its widespread adoption and the ease with which it allows us to manage microservices.

**MongoDB for Price Data Storage:**
MongoDB will store price data due to its built-in sharding and recovery capabilities, which are essential for handling large volumes of dynamic data. Its flexibility in handling unstructured data and strong support for horizontal scaling make it an ideal choice for this task. MongoDB ensures that price data is consistently available and quickly accessible for real-time updates.

**Java for Event Processing System:**
Java is our primary coding language for the event processing system due to our team's extensive familiarity with it. Java's strong performance, reliability, and robust ecosystem make it well-suited for building complex, high-performance applications. Using Java allows us to leverage our existing skills to efficiently develop and maintain the system.

**JavaScript for Market Pricing Calculator:**
JavaScript will be used for the market pricing calculator service, which recalculates the market value of each client's portfolio as asset prices fluctuate or transactions are made. JavaScript's ability to handle asynchronous operations and real-time updates makes it suitable for this task. The calculator must be highly scalable to process large volumes of updates and ensure clients always see the most up-to-date values.

**ZooKeeper for Concurrency and Fault Tolerance:**
ZooKeeper will be used to handle concurrency and ensure fault tolerance within our system. It will facilitate the quick assignment of work and ease of recovery by maintaining the state of our distributed systems. A backup leader node will be in place to ensure continuous operation and quick recovery in case of failures. ZooKeeper was chosen for its reliability and support for distributed coordination.

By leveraging these technologies, we aim to build a robust, scalable, and efficient system that meets our requirements for observability, market value calculation, and data processing.
