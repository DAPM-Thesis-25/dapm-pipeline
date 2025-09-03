package communication.message.serialization.deserialization;

import communication.message.Message;

public interface DeserializationStrategy {
    Message deserialize(String payload);
}
