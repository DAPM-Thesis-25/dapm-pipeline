package communication.message.serialization.deserialization.impl;

import communication.message.Message;
import communication.message.impl.Alignment;
import communication.message.impl.Trace;
import communication.message.serialization.parsing.JXESParser;
import communication.message.serialization.deserialization.DeserializationStrategy;

import java.util.List;
import java.util.Map;

public class AlignmentDeserializationStrategy implements DeserializationStrategy {

    /** Deserializes A JXES-formatted string into an alignment. Assumes the given string contains exactly 2 traces:
     *  The first trace is set to the log trace, and the second trace is set to the model trace. */
    @Override
    public Message deserialize(String payload) {
        Map<String, Object> jxesMap = (new JXESParser()).parse(payload);
        assert jxesMap.containsKey("traces") : "Incorrect payload format: unable to find \"traces\" key during jxes alignment deserialization";

        List<Trace> traces = (List<Trace>) jxesMap.get("traces");
        assert traces.size() == 2 : "Expected exactly 2 traces for alignment deserialization, but received: " + traces.size();
        return new Alignment(traces.get(0), traces.get(1));
    }

}
