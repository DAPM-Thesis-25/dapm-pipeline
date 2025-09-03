package communication.message;

import communication.message.serialization.MessageVisitor;
import communication.message.serialization.deserialization.DeserializationStrategy;
// TODO: make a guide on how to make a Message subclass (make DeserializationStrategy, add visitor, add to message_type schema, etc.)
public abstract class Message {
    public String getName() { return getClass().getName(); }

    public abstract void acceptVisitor(MessageVisitor<?> v);
}
