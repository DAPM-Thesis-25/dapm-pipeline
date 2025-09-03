package communication.message.impl.time;

import annotations.AutoRegisterMessage;
import communication.message.Message;
import communication.message.serialization.MessageVisitor;
import communication.message.serialization.deserialization.DeserializationStrategyRegistration;
import communication.message.serialization.deserialization.impl.UTCTimeDeserializationStrategy;

import java.time.Instant;

/** An absolute UTC time (a single instantaneous, unambiguous point on the time-line). */
@AutoRegisterMessage
@DeserializationStrategyRegistration(strategy = UTCTimeDeserializationStrategy.class)
public class UTCTime extends Message {
    private final Instant time;

    public UTCTime() {
        this.time = Instant.now();
    }

    public UTCTime(Instant instant) {
        this.time = instant;
    }

    public Instant getTime() { return time; }

    @Override
    public void acceptVisitor(MessageVisitor<?> messageVisitor) {
        messageVisitor.visit(this);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof UTCTime otherUTCTime)) return false;
        return time.equals(otherUTCTime.time);
    }

    @Override
    public int hashCode() { return time.hashCode(); }
}