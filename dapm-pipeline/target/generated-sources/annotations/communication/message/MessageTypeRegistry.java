package communication.message;

import java.util.HashMap;
import java.util.Map;

public class MessageTypeRegistry {
    private static final Map<String, Class<? extends Message>> nameToClass = new HashMap<>();

    static {
        register("Alignment", communication.message.impl.Alignment.class);
        register("Metrics", communication.message.impl.Metrics.class);
        register("Trace", communication.message.impl.Trace.class);
        register("Event", communication.message.impl.event.Event.class);
        register("PetriNet", communication.message.impl.petrinet.PetriNet.class);
        register("Date", communication.message.impl.time.Date.class);
        register("UTCTime", communication.message.impl.time.UTCTime.class);
    }

    public static boolean isSupportedMessageType(String simpleClassName) { return nameToClass.containsKey(simpleClassName); }

    public static Class<? extends Message> getMessageType(String simpleClassName) {
        if (!nameToClass.containsKey(simpleClassName)) { throw new IllegalArgumentException("Unknown message type: " + simpleClassName);}
        return nameToClass.get(simpleClassName);
    }

    private static void register(String simpleClassName, Class<? extends Message> messageClass) {
        nameToClass.put(simpleClassName, messageClass);
    }
}
