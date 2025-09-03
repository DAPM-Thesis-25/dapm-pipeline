package communication.message.impl;

import annotations.AutoRegisterMessage;
import communication.message.serialization.MessageVisitor;
import communication.message.Message;
import communication.message.impl.event.Event;
import communication.message.serialization.deserialization.DeserializationStrategyRegistration;
import communication.message.serialization.deserialization.impl.TraceDeserializationStrategy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@AutoRegisterMessage
@DeserializationStrategyRegistration(strategy=TraceDeserializationStrategy.class)
public class Trace extends Message implements Iterable<Event> {

    private final List<Event> trace;
    private String caseID;

    public Trace(List<Event> trace) {
        assert trace != null;
        this.trace = new ArrayList<>();
        for (Event event : trace) { add(event); }
    }

    public List<Event> getTrace() { return trace; }
    public String getCaseID() { return caseID; }

    @Override
    public void acceptVisitor(MessageVisitor<?> v) {
        v.visit(this);
    }

    public boolean add(Event event) {
        if (trace.isEmpty()) { this.caseID = event.getCaseID(); }
        assert event.getCaseID().equals(caseID) : String.format("All events in a trace must have the same case ID. (TraceCID, givenCID) = (\"%s\", \"%s\")", caseID, event.getCaseID());
        return trace.add(event);
    }

    public int size() { return trace.size(); }

    public boolean isEmpty() { return trace.isEmpty(); }

    @Override
    public String toString() {
        return "T[" + caseID + ':' + trace.toString() + "]";
    }

    @Override
    public Iterator<Event> iterator() { return trace.iterator(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Trace otherTrace)) return false;
        return trace.equals(otherTrace.trace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(caseID, trace);
    }
}