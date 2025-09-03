package pipeline.processingelement;

import communication.Consumer;
import communication.Subscriber;
import communication.message.Message;
import utils.Pair;

import java.util.HashMap;
import java.util.Map;

public abstract class ConsumingProcessingElement extends ProcessingElement implements Subscriber<Pair<Message, Integer>> {
    /** Holds the input types and their multiplicities. So if processing element consumes Event's from two
     *  separate Channel's and Petri Net's from one Channel, it will have (key, value) pairs (Event.class, 2) and
     *  (PetriNet.class, 1).*/
    protected final Map<Class<? extends Message>, Integer> inputs;
    private final Map<Integer, Consumer> consumers = new HashMap<>();

    public ConsumingProcessingElement(Configuration configuration) {
        super(configuration);
        this.inputs = setConsumedInputs();
        for (int typeCount : inputs.values()) {
            assert typeCount > 0 : "every provided input type must occur a positive number of times.";
        }
    }

    protected Map<Integer, Consumer> getConsumers() { return Map.copyOf(consumers);}

    protected abstract Map<Class<? extends Message>, Integer> setConsumedInputs(); // TODO: refactor with annotations

    @Override
    public boolean start() {
        boolean started = true;
        for (Consumer consumer : consumers.values()) {
            started &= consumer.start();
        }
        return started;
    }

    @Override
    public boolean terminate() {
        if (!stop())
            { return false; }

        boolean terminated = true;
        for (Consumer consumer : consumers.values()) {
           terminated &= consumer.terminate();
        }
        if(terminated) consumers.clear();
        return terminated;
    }

    private boolean stop() {
        boolean stopped = true;
        for (Consumer consumer : consumers.values()) {
            stopped &= consumer.stop();
        }
        return stopped;
    }

    public final void registerConsumer(Consumer consumer, int portNumber) {
        consumers.put(portNumber, consumer);
    }
}
