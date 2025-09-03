package communication;

import communication.config.ConsumerConfig;
import communication.message.Message;
import communication.message.serialization.deserialization.MessageFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.config.MethodKafkaListenerEndpoint;

import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.stereotype.Component;
import utils.IDGenerator;
import utils.Pair;

import java.util.HashMap;
import java.util.Map;


@Component
@Scope("prototype")
public class Consumer {
    private final KafkaListenerEndpointRegistry registry;
    private final ApplicationContext applicationContext;

    private Subscriber<Pair<Message, Integer>> subscriber;
    private int portNumber;
    private String containerId;
    private String brokerUrl;
    private String topic;

    @Autowired
    public Consumer(KafkaListenerEndpointRegistry registry, ApplicationContext applicationContext) {
        this.registry = registry;
        this.applicationContext = applicationContext;
    }

    public void registerListener(Subscriber<Pair<Message, Integer>> subscriber, ConsumerConfig consumerConfig) {
        this.subscriber = subscriber;
        this.portNumber = consumerConfig.portNumber();
        this.containerId = IDGenerator.generateKafkaContainerID();
        this.brokerUrl = consumerConfig.brokerURL();
        this.topic = consumerConfig.topic();
        MethodKafkaListenerEndpoint<String, String> endpoint = null;
        try {
            endpoint = new MethodKafkaListenerEndpoint<>();
            endpoint.setId(containerId);
            endpoint.setGroupId(IDGenerator.generateConsumerGroupID());
            endpoint.setTopics(consumerConfig.topic());
            endpoint.setBean(this);
            endpoint.setMethod(
                    Consumer.class.getDeclaredMethod("observe", ConsumerRecord.class, Acknowledgment.class)
            );
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Failed to register Kafka listener endpoint", e);
        }
        endpoint.setMessageHandlerMethodFactory(applicationContext.getBean(MessageHandlerMethodFactory.class));

        KafkaListenerContainerFactory<?> factory = createConsumerContainerFactory(consumerConfig.brokerURL());
        registry.registerListenerContainer(endpoint, factory, false);
    }

    public void observe(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            Message msg = MessageFactory.deserialize(record.value());
            this.subscriber.observe(new Pair<>(msg, portNumber));
            ack.acknowledge(); // At-least-once guarantee
        } catch (Exception e) {
            throw new RuntimeException("Failed to process message" + record, e);
        }
    }

    public boolean start() {
        var container = registry.getListenerContainer(containerId);
        assert container != null;
        container.start();
        return container.isRunning();
    }

    public boolean stop() {
        var container = registry.getListenerContainer(containerId);
        assert container != null;
        container.stop();
        return !container.isRunning();
    }

    public boolean terminate() {
        var container = registry.getListenerContainer(containerId);
        assert container != null;
        registry.unregisterListenerContainer(containerId);
        return registry.getListenerContainer(containerId) == null;
    }

    private KafkaListenerContainerFactory<?> createConsumerContainerFactory(String brokerUrl) {
        Map<String, Object> props = new HashMap<>();
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // At-least-once guarantee

        ConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(props);

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(1); // Only one working thread per container
        factory.getContainerProperties().setPollTimeout(1000);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public String getTopic() {
        return topic;
    }
}

