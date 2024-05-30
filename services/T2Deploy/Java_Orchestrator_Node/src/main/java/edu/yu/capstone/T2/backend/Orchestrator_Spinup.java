package edu.yu.capstone.T2.backend;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;

import java.util.Properties;

public class Orchestrator_Spinup {

    public static void main(String[] args) {
        String rabbitURI = System.getenv("RABBITMQ_URI");
        String serviceQueueName = System.getenv("SERVICE_QUEUE_NAME");
        String portfolioQueueName = System.getenv("PORTFOLIO_QUEUE_NAME");
        String stockQueueName = System.getenv("STOCK_QUEUE_NAME");
        String routingKeyStart = System.getenv("ROUTING_KEY_START");
        String exchangeName = System.getenv("EXCHANGE_NAME");
        String rabbitmq_host = System.getenv("RABBITMQ_HOST");
        String outside_docker = System.getenv("OUTSIDE_DOCKER");

        if (rabbitURI == null || serviceQueueName == null || portfolioQueueName == null || stockQueueName == null
                || routingKeyStart == null || exchangeName == null) {
            System.out.println("Missing required arguments.");
            System.exit(1);
        }

        // Now let's create the RabbitMQ connection and consume messages from the Trade
        // Service
        ConnectionFactory connectionFactory = new ConnectionFactory();
        try {
            // 2 producers, 3 consumers

            if (outside_docker != null) {
                connectionFactory.setPort(5672);
                connectionFactory.setHost("localhost");
                connectionFactory.setUsername("guest");
                connectionFactory.setPassword("guest");
                System.out.println("Rabbit Trade-service Factory setup for outside docker");
            } else {
                connectionFactory.setPort(5672);
                connectionFactory.setHost(rabbitmq_host);
                connectionFactory.setUsername("guest");
                connectionFactory.setPassword("guest");
                System.out.println("Rabbit Trade-service Factory setup for inside docker");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        Orchestrator_Node node = new Orchestrator_Node(connectionFactory, serviceQueueName, stockQueueName,
                portfolioQueueName, routingKeyStart, exchangeName);

        node.run();
    }
}
