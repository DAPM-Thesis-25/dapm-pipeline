package communication.message.impl.event;

import annotations.AutoRegisterMessage;
import communication.message.Message;
import communication.message.serialization.MessageVisitor;
import communication.message.serialization.deserialization.DeserializationStrategyRegistration;
import communication.message.serialization.deserialization.impl.EventDeserializationStrategy;

import java.util.Collection;
import java.util.Set;

@AutoRegisterMessage
@DeserializationStrategyRegistration(strategy = EventDeserializationStrategy.class)
public class Event extends Message {
    private final String caseID;
    private final String activity;
    private final String timestamp;
    private final Set<Attribute<?>> attributes;

    // Note that there deliberately is no constructor other constructor than this one. This is to emphasize that an
    // event is atomic, and therefore e.g. attributes cannot be added after the event's digital twin's initialization.
    public Event(String caseID, String activity, String timestamp, Set<Attribute<?>> attributes) {
        assert caseID != null && activity != null && timestamp != null;
        this.caseID = caseID;
        this.activity = activity;
        this.timestamp = timestamp;
        this.attributes = attributes;
    }

    public String getCaseID() {return caseID;}
    public String getActivity() {return activity; }
    public String getTimestamp() {return timestamp; }
    public Collection<Attribute<?>> getAttributes() {return attributes;}

    @Override
    public void acceptVisitor(MessageVisitor<?> v) {
        v.visit(this);
    }

    @Override
    public String toString() {
        return String.format("Event [caseID=%s, activity=%s, timestamp=%s, attributes=%s]",caseID,activity,timestamp,attributes);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof Event otherEvent)) return false;
        return caseID.equals(otherEvent.getCaseID())
                && activity.equals(otherEvent.getActivity())
                && timestamp.equals(otherEvent.getTimestamp())
                && attributes.equals(otherEvent.attributes);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(caseID, activity, timestamp, attributes);
    }
}