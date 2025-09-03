package communication.config;


public record ConsumerConfig(String brokerURL, String topic, int portNumber){}