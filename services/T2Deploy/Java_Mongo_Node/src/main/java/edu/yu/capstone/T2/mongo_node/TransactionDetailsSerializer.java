package edu.yu.capstone.T2.mongo_node;

import org.apache.kafka.common.serialization.Serializer;

import java.io.IOException;
import java.util.Map;

public class TransactionDetailsSerializer implements Serializer<TransactionDetails> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // Nothing to configure in this case
    }

    @Override
    public byte[] serialize(String topic, TransactionDetails data) {
        if (data == null)
            return null;

        try {
            return data.serialize(); // This calls the serialize method in TransactionDetails
        } catch (IOException e) {
            throw new RuntimeException("Error serializing TransactionDetails", e);
        }
    }

    @Override
    public void close() {
        // Nothing to close in this case
    }
}
