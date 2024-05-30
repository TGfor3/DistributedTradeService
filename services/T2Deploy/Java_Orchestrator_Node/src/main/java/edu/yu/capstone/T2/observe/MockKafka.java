package edu.yu.capstone.T2.observe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockKafka {
    private Map<String, List<String>> topics;

    public MockKafka() {
        topics = new HashMap<>();
    }

    public void createTopic(String topicName) {
        topics.putIfAbsent(topicName, new ArrayList<>());
    }

    public void send(String topicName, String message) {
        List<String> messages = topics.get(topicName);
        if (messages != null) {
            messages.add(message);
        }
    }

    public List<String> getMessages(String topicName) {
        return topics.getOrDefault(topicName, new ArrayList<>());
    }
}