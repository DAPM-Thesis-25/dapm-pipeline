package pipeline.processingelement.source;

import communication.Producer;
import communication.ProducingProcessingElement;
import communication.Publisher;
import communication.message.Message;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.ProcessingElement;

public abstract class Source<O extends Message> extends ProcessingElement implements Publisher<O>, ProducingProcessingElement {
    private Producer producer; // Channel

    public Source(Configuration configuration) { super(configuration); }

    @Override
    public final void publish(O data) {
        producer.publish(data);
    }

    @Override
    public abstract boolean start();

    @Override
    public boolean terminate() {
        if (!producer.stop())
            { return false; }
        boolean terminated = producer.terminate();
        if (terminated) producer = null;
        return terminated;
    }

    public final void registerProducer(Producer producer) {
        this.producer = producer;
    }
}
