package communication;

import communication.config.ConsumerConfig;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Component;
import pipeline.processingelement.ConsumingProcessingElement;

@Component
public abstract class ConsumerFactory {

    @Lookup
    protected abstract Consumer createMessageListener();

    public void registerConsumer(ConsumingProcessingElement subscriber, ConsumerConfig config) {
        Consumer consumer = createMessageListener();
        consumer.registerListener(subscriber, config);
        subscriber.registerConsumer(consumer, config.portNumber());
    }
}