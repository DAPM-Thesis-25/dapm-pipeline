package communication.message.serialization;

import communication.message.impl.Alignment;
import communication.message.impl.Metrics;
import communication.message.impl.time.UTCTime;
import communication.message.impl.time.Date;
import communication.message.impl.Trace;
import communication.message.impl.event.Event;
import communication.message.impl.petrinet.PetriNet;

public interface MessageVisitor<T> {
    T visit(Event e);
    T visit(PetriNet pn);
    T visit(Trace t);
    T visit(Alignment a);
    T visit(Date time);
    T visit(UTCTime UTCTime);
    T visit(Metrics metrics);
}
