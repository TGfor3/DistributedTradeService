# Scope and Use Cases


## Realtime prices

### Accepting Stock Price Updates

**Description:**
This section describes the functionality of the system regarding the acceptance of stock price updates from various data sources.

**Trigger:**
Stock price updates are received from data sources via the provided API endpoint.

**Basic Flow:**
1. Data sources send stock price updates to the designated API endpoint.
2. The message queue receives the updates and publishes them to the appropriate topic
3. The compacted view of the topic is updated to reflect the new message


**Postcondition:**
The compacted view of the stock price is updated with the latest stock price information, ensuring data accuracy and consistency.

---

### Providing a Realtime View of Stock Prices

**Description:**
This section outlines how the system provides a realtime view of stock prices to clients.

**Trigger:**
Clients request stock price information via the provided API endpoint.

**Basic Flow:**
1. Clients send requests for stock price information to the designated API endpoint.
2. The server handling the requests  retrieves the data from the compacted view of the relevant topics.
3. Retrieved data is formatted and sent back to the clients in realtime.
4. Clients receive and display the realtime stock price information.



**Postcondition:**
Clients receive and display the requested realtime stock price information, facilitating informed decision-making.

---

### Providing a Framework for Managing an In-Memory Cache

**Description:**
This section describes the framework provided by the system for managing an in-memory cache on client machines to enhance throughput and reduce network requests.

**Trigger:**
Clients retrieve stock price information using the provided package of code.

**Basic Flow:**
1. Clients utilize the provided package of code to retrieve stock price information.
2. The package manages an in-memory cache on the client machine and pro-actively fetches frequently accessed stock price data.
3. When clients request stock price information, the system first checks the cache.
4. If the requested data is available in the cache, it is returned to the clients directly from the cache, reducing network requests.
5. If the requested data is not available in the cache, the system retrieves it from the database, updates the cache, and returns the data to the clients.

**Alternative Flow:**
- If the cache is full or exceeds a predefined threshold, the system may employ cache eviction strategies to make room for new data.

**Postcondition:**
Clients benefit from improved throughput and reduced network requests due to the efficient management of an in-memory cache, enhancing overall system performance.

# Historical Price Trends

### Stock Price Update Reception

**Trigger:** A data source sends a stock price update to the API endpoint.

**Description:** Data sources send stock price updates which are then processed and stored for future retrieval.

**Preconditions:**
- The system is capable of receiving and processing stock price updates.
- Stock price updates are sent in a supported format.

**Basic Flow:**
1. A data source sends a stock price update to the provided API endpoint.
2. The system validates the format of the update.
3. The update is then queued for processing to handle the load efficiently.
4. The processing node extracts relevant information from the update.
5. The extracted data is stored in the long-term database with appropriate indexing for efficient querying.
6. The system updates the internal cache to reflect the latest stock price for immediate access.
7. A confirmation is sent back to the data source acknowledging the receipt and processing of the update.

**High Availability Element:**
- The system uses a distributed architecture to ensure high availability. If a node fails, other nodes in the cluster continue to serve clients without interruption.
- Data is replicated across multiple nodes to prevent data loss and to facilitate an eventually consistent view of stock prices.

### Historical Trend Retrieval

**Trigger:** A system component requests historical stock price trends.

**Description:** Clients or internal components request historical stock price data for analysis or decision-making purposes.

**Preconditions:**
- The system has stored historical stock price data.
- The request specifies the stock symbol and the time range for which the data is needed.

**Basic Flow:**
1. A request is made to the API endpoint for historical stock price data.
2. The system validates the request parameters.
3. The system queries the long-term database using the specified parameters.
4. Data is retrieved and formatted according to the request (e.g., daily averages, monthly highs and lows).
5. The system sends the requested data back to the requester.

**Scalability Element:**
- The system is designed to be horizontally scalable, allowing it to handle a large number of requests for historical data.
- As demand increases, more nodes can be added to the cluster to distribute the load and improve performance.

### Eventual Consistency and Order Guarantee

**Description:** The system ensures that all updates are eventually reflected in the view provided to clients and maintains the chronological order of updates.

**Trigger:** Continuous inflow of stock price updates.

**Preconditions:**
- Stock price updates are being received and processed by the system.

**Basic Flow:**
1. Updates are received and timestamped to maintain order.
2. The system processes updates in the order they are received, ensuring chronological integrity.
3. In case of high latency or node failure, the system ensures no updates are lost and are eventually processed.
4. The consistency model guarantees that once an update is processed, all subsequent retrievals of stock data reflect this update.

**Alternative Flows:**
- If updates from the same source arrive out of order, the system queues them to be processed in the correct chronological order.
- In the event of a node failure, the system redistributes the load to other nodes without losing any updates.

**Postconditions:**
- The system provides an eventually consistent view of stock price updates.
- The chronological order of updates is preserved, ensuring that later updates are not visible before earlier ones.

These scenarios outline the scope and use cases for your project, focusing on the reception of stock price updates, processing for historical trends, maintaining high availability, scalability, eventual consistency, and the order guarantee of updates.

# Heartbeat System Deployment

## Overview

This document outlines the deployment and operational details of the Heartbeat System within a distributed system. The Heartbeat System is crucial for monitoring the health and availability of all nodes, leveraging a centralized Kafka message queue for reporting.

## Trigger

- **Deployment or Restart**: Triggered when a new node is deployed within the distributed system or an existing node starts/stops sending heartbeats.

## Description

The process ensures continuous monitoring of node health and availability, utilizing heartbeat signals to detect active and inactive nodes. This data is reported through a Kafka message queue and analyzed via Elastic DB for comprehensive system health insights.

## Preconditions

- The heartbeat system package is installed and configured on all nodes.
- Kafka message queue is operational and accessible by all nodes.
- Elastic DB is set up for data aggregation and analysis.

## Basic Flow

1. **Heartbeat Sending**: Each node sends periodic heartbeat signals to indicate its operational status.
2. **Collection and Analysis**: A leader node or monitoring service collects these heartbeats, identifying inactive nodes and publishing their status to the Kafka queue.
3. **Data Processing**: The analytics and reporting service consumes these messages, processing and storing the information in Elastic DB.
4. **Reporting**: Provides API endpoints for querying health status and performance metrics, with REST API calls available for system health and performance reports.

## Node Failure Detection and Alerting

### Description

Covers the detection of node failures, alerting, and reporting mechanisms for system administrators or automated recovery systems.

### Trigger

- A node stops sending heartbeat signals for a predefined period.

### Preconditions

- Active monitoring of all nodes by the heartbeat system.
- Kafka message queue and Elastic DB configured for messaging and data analysis.

### Basic Flow

1. **Failure Detection**: The monitoring service detects a node's failure to send heartbeat signals.
2. **Alerting**: An alert message is published to Kafka, with the analytics service updating the Elastic DB and generating notifications.
3. **Recovery and Logging**: The system logs and reports are updated, with notifications for temporary downtimes or recoveries.

### Alternative Flows

- **Temporary Downtime**: If a node resumes sending heartbeats before alert escalation, the system updates its status and notifies administrators.

## System Performance Monitoring

### Description

Monitors system performance metrics to ensure optimal operation and identify bottlenecks.

### Trigger

- Periodic collection of performance metrics from nodes, including CPU, memory, network info, and other system info.

### Preconditions

- Nodes configured to report performance metrics.
- Kafka message queue and Elastic DB for data aggregation and analysis.

### Basic Flow

1. **Metric Collection**: Nodes publish performance metrics to Kafka at regular intervals.
2. **Data Processing**: The analytics service processes these metrics, with data aggregated and stored in Elastic DB.
3. **Performance Reporting**: Provides REST API endpoints for querying metrics, generating regular system performance reports.

### Alternative Flows

- **Anomaly Detection**: Alerts for performance anomalies and advanced analytics for predicting potential downtimes or degradations.

## Postconditions

- Accurate, up-to-date health and performance view of all nodes.
- System administrators have access to detailed reports and alerts.
- Prompt identification and resolution of potential issues, ensuring high system availability and performance.

