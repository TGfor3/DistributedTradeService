package edu.yu.capstone.T2.observe;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

public class EventManager {
    private KafkaProducer<String, String> producer;
    private MockKafka mockKafka;
    private String machineId;

    public EventManager(String kafkaUrl, String machineId) {
        this(kafkaUrl, machineId, false);
    }

    public EventManager(MockKafka mockKafka, String machineId) {
        this(null, machineId, true);
        this.mockKafka = mockKafka;
    }

    private EventManager(String kafkaUrl, String machineId, boolean useMockKafka) {
        if (!useMockKafka && (kafkaUrl == null || kafkaUrl.isEmpty())) {
            throw new IllegalArgumentException("Kafka URL is required");
        }
        if (machineId == null || machineId.isEmpty()) {
            throw new IllegalArgumentException("Machine ID is required");
        }

        this.machineId = machineId;

        if (!useMockKafka) {
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaUrl);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            this.producer = new KafkaProducer<>(props);
        }

        System.out.println("This machine id: " + this.machineId);
    }

    public void sendEvent(String poolId, long eventId, String eventKey, double eventValue) {
        try {
            long timestamp = System.currentTimeMillis();
            String fullMsg = String.format(
                    "{\"poolid\":\"%s\",\"machineid\":\"%s\",\"eventid\":%d,\"eventkey\":\"%s\",\"eventvalue\":%f,\"timestamp\":%d}",
                    poolId, this.machineId, eventId, eventKey, eventValue, timestamp);
            if (mockKafka != null) {
                mockKafka.send("OBSERVABILITY_EVENTS_TOPIC", fullMsg);
            } else {
                ProducerRecord<String, String> record = new ProducerRecord<>("ObservabilityTopic", poolId, fullMsg);
                producer.send(record);
            }
            System.out.println("SENDING: " + fullMsg);
        } catch (Exception e) {
            System.err.println("Error publishing message: " + e.getMessage());
        }
    }

    public void send1SecondCPUUsage(String poolId) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                double cpuUsage = getCPUUsage();
                sendEvent(poolId, new Random().nextLong(), "CPU_Usage", cpuUsage * 100);
            }
        }, 1000);
    }

    public void sendMemoryUsage(String poolId) {
        com.sun.management.OperatingSystemMXBean osBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory
                .getOperatingSystemMXBean();
        double freeMemory = osBean.getFreePhysicalMemorySize();
        double totalMemory = osBean.getTotalPhysicalMemorySize();
        double memoryUsage = (1 - freeMemory / totalMemory) * 100;
        sendEvent(poolId, new Random().nextLong(), "Memory_Usage", memoryUsage);
    }

    private double getCPUUsage() {
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        return osBean.getSystemLoadAverage() / osBean.getAvailableProcessors();
    }
}
