# Set the final base image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the executable JAR file from the build stage to the new image
# COPY --from=build /app/target/Java_Orchestrator_Node-1.0-SNAPSHOT-jar-with-dependencies.jar .
COPY ./target/Java_Orchestrator_Node-1.0-SNAPSHOT-jar-with-dependencies.jar /app/Java_Orchestrator_Node-1.0-SNAPSHOT-jar-with-dependencies.jar

# Specify the command to run on container start
CMD ["java", "-jar", "Java_Orchestrator_Node-1.0-SNAPSHOT-jar-with-dependencies.jar", "-verbose:class"]

