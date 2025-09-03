package communication.message.serialization.deserialization.impl;

import communication.message.Message;
import communication.message.impl.Trace;
import communication.message.serialization.parsing.JXESParser;
import communication.message.serialization.deserialization.DeserializationStrategy;

import java.util.List;
import java.util.Map;

public class TraceDeserializationStrategy implements DeserializationStrategy {
    @Override
    public Message deserialize(String payload) {
        Map<String, Object> jxesMap = (new JXESParser()).parse(payload);
        assert jxesMap.containsKey("traces") : "incorrect payload format: can't find the \"traces\" key.";

        List<Trace> traces = (List<Trace>) jxesMap.get("traces");
        assert traces.size() == 1 : "Expected exactly 1 trace during trace deserialization, but received: " + traces.size();

        return traces.getFirst();
    }

}
