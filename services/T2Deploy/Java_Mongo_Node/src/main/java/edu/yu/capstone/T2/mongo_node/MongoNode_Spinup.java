package edu.yu.capstone.T2.mongo_node;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import edu.yu.capstone.T2.mongo_node.MongoNode.CollectionType;
import edu.yu.capstone.T2.mongo_node.impl.TransactionImpl;

import java.util.Properties;
import java.util.logging.Logger;

public class MongoNode_Spinup {

    public static void main(String[] args) {
        // Read environment variables
        String mongoURI = System.getenv("MONGO_URI");
        String rabbitURI = System.getenv("RABBIT_URI");
        String rabbitQueueName = System.getenv("RABBIT_QUEUE_NAME");
        String generalRoutingKey = System.getenv("ROUTING_KEY_START");
        String nodeTypeEnv = System.getenv("NODE_TYPE");
        Logger logger = Logger.getLogger(MongoNode_Spinup.class.getName());
        String exchangeName = System.getenv("EXCHANGE_NAME");
        String outsideDocker = System.getenv("OUTSIDE_DOCKER");
        String rabbit_host = System.getenv("RABBITMQ_HOST");
        String kafkaBootstrapServers = System.getenv("KAFKA_BOOTSTRAP_SERVERS");

        TransactionImpl transaction;
        MongoNode.NodeType nodeType = null;
        CollectionType collectionType = null;

        // Parse nodeType from environment variable
        if (nodeTypeEnv != null) {
            switch (nodeTypeEnv) {
                case "BROKERAGE_COLLECTION":
                    nodeType = MongoNode.NodeType.BROKERAGE_COLLECTION;
                    break;
                case "CLIENT_COLLECTION":
                    nodeType = MongoNode.NodeType.CLIENT_COLLECTION;
                    break;
                default:
                    throw new IllegalArgumentException(
                            "NODE_TYPE environment variable must be either 'BROKERAGE_COLLECTION' or 'CLIENT_COLLECTION'");
            }
        }
        if (mongoURI == null) {
            logger.severe("MONGO_URI environment variable is missing.");
            System.exit(1);
        }
        if (rabbitURI == null) {
            logger.severe("RABBIT_URI environment variable is missing.");
            System.exit(1);
        }
        if (rabbitQueueName == null) {
            logger.severe("RABBIT_QUEUE_NAME environment variable is missing.");
            System.exit(1);
        }
        if (generalRoutingKey == null) {
            logger.severe("ROUTING_KEY_START environment variable is missing.");
            System.exit(1);
        }
        if (exchangeName == null) {
            logger.severe("EXCHANGE_NAME environment variable is missing.");
            System.exit(1);
        }

        // Set the factory
        ConnectionFactory factory = new ConnectionFactory();
        try {

            if (outsideDocker != null) {
                factory.setPort(5672);
                factory.setHost("localhost");
                factory.setUsername("guest");
                factory.setPassword("guest");
            } else {
                factory.setPort(5672);
                factory.setHost(rabbit_host);
                factory.setUsername("guest");
                factory.setPassword("guest");
            }

        } catch (Exception e) {
            logger.info("Rabbit has failed and not been set up properly \n");
            Thread.interrupted();
            e.printStackTrace();
            System.exit(1);
        }

        Properties kafkaProperties = new Properties();
        kafkaProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        kafkaProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        kafkaProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(kafkaProperties);

        // Initialize RepositoryPatternBase for the Node
        transaction = new TransactionImpl(mongoURI, kafkaProducer);

        MongoNode node = new MongoNode(factory, rabbitQueueName, generalRoutingKey, exchangeName, transaction, nodeType,
                collectionType);

        node.run();
    }
}
