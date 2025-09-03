package utils;

import java.util.UUID;

public class IDGenerator {
    private static int nextID = 0;

    public static int generate() { return nextID++; }

    public static String generateTopic() {
        return "Topic-" + UUID.randomUUID();
    }

    public static String generateInstanceID() {
        return "Instance-" + UUID.randomUUID();
    }

    public static String generateKafkaContainerID() {
        return "Container-" + UUID.randomUUID();
    }

    public static String generateConsumerGroupID() {
        return "ConsumerGroup-" + UUID.randomUUID();
    }
}
