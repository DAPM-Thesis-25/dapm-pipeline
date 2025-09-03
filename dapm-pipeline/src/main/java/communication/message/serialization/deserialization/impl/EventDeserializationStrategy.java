package communication.message.serialization.deserialization.impl;

import communication.message.impl.Trace;
import communication.message.serialization.parsing.JXESParser;
import communication.message.Message;
import communication.message.serialization.deserialization.DeserializationStrategy;

import java.util.List;
import java.util.Map;

public class EventDeserializationStrategy implements DeserializationStrategy {

    /** Deserializes a JXES-formatted string into an event. Assumes the string contains only a single event.
     *  Currently, this method does not support classifier values; the case ID will be the "concept:name" key's value in
     *  "traces" -> "attrs" or in the global-attrs (prioritizing the first over the latter), and the activity will be
     *  the value of the "concept:name" key in an event. Note also that a trace's "attrs" keys will also be added to the
     *  returned event's attributes. */
    @Override
    public Message deserialize(String payload) {
        Map<String, Object> jxesMap = (new JXESParser()).parse(payload);
        printUnusedAttributes(jxesMap);
        assert jxesMap.containsKey("traces") : "Incorrect payload format: an event must have the \"traces\" key set";

        List<Trace> traces = (List<Trace>) jxesMap.get("traces");
        assert traces.size() == 1 : "An event is assumed to have exactly one trace";

        Trace trace = traces.getFirst();
        assert trace.size() == 1 : "Exactly 1 event was expected, but the trace contained: " + trace.size();
        return trace.iterator().next();
    }

    private void printUnusedAttributes(Map<String, Object> jxesMap) {
        for (String key : List.of("log-properties", "log-attrs", "extensions", "classifiers")) {
            if (jxesMap.containsKey(key)) {
                System.out.println('\"' + key + "\" JXES key not currently used for event deserialization.");
            }
        }
    }

}
