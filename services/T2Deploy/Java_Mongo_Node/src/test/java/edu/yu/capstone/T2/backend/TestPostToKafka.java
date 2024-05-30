package edu.yu.capstone.T2.backend;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import edu.yu.capstone.T2.mongo_node.TransactionDetails;
import edu.yu.capstone.T2.mongo_node.TransactionDetails.CompletionType;
import edu.yu.capstone.T2.mongo_node.TransactionDetails.TransactionType;
import edu.yu.capstone.T2.mongo_node.impl.TransactionImpl;

public class TestPostToKafka {
    public static void main(String[] args) {
        String mongoURL = "mongodb://isigutt:isi@192.168.8.224:27017/";
        String kafkaBootstrapServers = "192.168.8.224:9092";

        Properties kafkaProperties = new Properties();
        kafkaProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        kafkaProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        kafkaProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(kafkaProperties);

        TransactionImpl impl = new TransactionImpl(mongoURL, kafkaProducer);
        TransactionDetails tx = new TransactionDetails(100, "CASH", 100, TransactionType.BUY, 100, 10,
                CompletionType.UNFINISHED);

        // kafkaProducer.close();

        impl.postToKafka(tx);
    }
}
