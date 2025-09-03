package communication.message.serialization.deserialization.impl;

import communication.message.Message;
import communication.message.impl.time.UTCTime;
import communication.message.serialization.deserialization.DeserializationStrategy;

import java.time.Instant;

public class UTCTimeDeserializationStrategy implements DeserializationStrategy {
    @Override
    public Message deserialize(String payload) {
        return new UTCTime(Instant.parse(payload));
    }
}