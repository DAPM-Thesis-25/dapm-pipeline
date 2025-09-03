package pipeline.processingelement.operator;

import communication.Producer;
import communication.ProducingProcessingElement;
import communication.message.Message;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.ConsumingProcessingElement;
import communication.Publisher;
import utils.Pair;

public abstract class Operator<AO, O extends Message> extends ConsumingProcessingElement
        implements Publisher<O>, ProducingProcessingElement {
    private Producer producer;

    public Operator(Configuration configuration) { super(configuration); }

    @Override
    public final void observe(Pair<Message, Integer> inputAndPortNumber) {
        AO algorithmOutput = process(inputAndPortNumber.first(), inputAndPortNumber.second());
        if (publishCondition(algorithmOutput)) {
            O output = convertAlgorithmOutput(algorithmOutput);
            publish(output);
        }
    }

    protected abstract AO process(Message input, int portNumber);

    protected abstract O convertAlgorithmOutput(AO algorithmOutput);

    protected abstract boolean publishCondition(AO algorithmOutput);

    @Override
    public final void publish(O data) {
        producer.publish(data);
    }

    @Override
    public boolean terminate() {
        if (!super.terminate()) // terminate consumers
            { return false; }
        boolean terminated = producer.terminate();
        if (terminated) producer = null;
        return terminated;
    }

    public final void registerProducer(Producer producer) {
        this.producer = producer;
    }
}
