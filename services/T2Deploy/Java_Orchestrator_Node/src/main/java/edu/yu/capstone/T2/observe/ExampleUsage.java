package edu.yu.capstone.T2.observe;

import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class ExampleUsage {
    public static void main(String[] args) {
        MockKafka mockKafka = new MockKafka();
        mockKafka.createTopic("OBSERVABILITY_EVENTS_TOPIC");

        EventManager eventManager = new EventManager(mockKafka, "machine-1");
        String poolId = "test-pool";

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                eventManager.send1SecondCPUUsage(poolId);
                eventManager.sendMemoryUsage(poolId);
                eventManager.sendEvent(poolId, new Random().nextLong(), "testKey", new Random().nextDouble() * 100);
            }
        }, 0, 1000);

        // Wait for some time to allow messages to be sent
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Retrieve and print the messages from the mock Kafka
        List<String> messages = mockKafka.getMessages("OBSERVABILITY_EVENTS_TOPIC");
        for (String message : messages) {
            System.out.println("Received message: " + message);
        }
    }
}