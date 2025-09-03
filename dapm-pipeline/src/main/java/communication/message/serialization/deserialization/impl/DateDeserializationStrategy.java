package communication.message.serialization.deserialization.impl;

import communication.message.Message;
import communication.message.impl.time.Date;
import communication.message.serialization.deserialization.DeserializationStrategy;

import java.time.ZonedDateTime;

public class DateDeserializationStrategy implements DeserializationStrategy {
    @Override
    public Message deserialize(String s) {
        return new Date(ZonedDateTime.parse(s));
    }
}
