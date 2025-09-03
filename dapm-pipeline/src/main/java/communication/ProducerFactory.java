package communication;

import communication.config.ProducerConfig;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Component;

@Component
public abstract class ProducerFactory {

    @Lookup
    protected abstract Producer createMessageProducer();

    public void registerProducer(ProducingProcessingElement publisher, ProducerConfig producerConfig) {
        Producer producer = createMessageProducer();
        producer.registerPublisher(producerConfig);
        publisher.registerProducer(producer);
    }
}
