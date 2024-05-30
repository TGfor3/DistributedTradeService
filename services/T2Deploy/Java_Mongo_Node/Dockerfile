# # Use an official Maven runtime as a parent image
# FROM maven:3.8.4-openjdk-17-slim AS build

# # Set the working directory in the container
# WORKDIR /app

# # Copy the Maven project Object Model (POM) file to the container
# COPY pom.xml .

# # Optionally, resolve dependencies as a separate step to improve the build cache
# RUN mvn dependency:resolve

# # Copy the source code to the container
# COPY src ./src

# # Build the application
# RUN mvn clean package

# Set the final base image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the executable JAR file from the build stage to the new image
COPY ./target/Java_Mongo_Node-1.0-SNAPSHOT-jar-with-dependencies.jar /app/Java_Mongo_Node-1.0-SNAPSHOT-jar-with-dependencies.jar


# Specify the command to run on container start
CMD ["java", "-jar", "Java_Mongo_Node-1.0-SNAPSHOT-jar-with-dependencies.jar", "-verbose:class"]

#PORTFOLIO_QUEUE_NAME=clientAccQueue;RABBITMQ_URI=amqp://rabbit_mq?connection_attempts=10&retry_delay=10;ROUTING_KEY_START=capstoneRabbit;STOCK_QUEUE_NAME=brokerInvQueue;NODE_TYPE=BROKERAGE_COLLECTION

