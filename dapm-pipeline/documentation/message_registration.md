# Registering a Message Type
The data flowing through the pipeline stem from `Message` objects. `Message` is an abstract class from which concrete message types are derived, such as `Event` or `PetriNet`. The set of supported message types can be extended. This guide explains how to register a new message type.

### 1. Create a New Message Class
Create and define a new class that extends `Message`. In the following examples, we'll call the new class `MYMESSAGE`. While not strictly necessary, we recommend placing it within the `communication.message.impl` package.

### 2. Add the AutoRegisterMessage Annotation
Add the `@AutoRegisterMessage` annotation to the class:
``` java
annotations.AutoRegisterMessage;

@AutoRegisterMessage
public class MYMESSAGE extends Message {
    // Class implementation
}
```

This annotation will be used by the annotation-processor to automatically register the class in the `MessageTypeRegistry`. Later, when running "mvn clean install" from dapm-pipeline, the `MessageTypeRegistry` will be generated during compilation. After compilation, it can be found in:

``` 
target/generated-sources/annotations/communication/message/MessageTypeRegistry.java
```
### 3. Override the acceptVisitor Method
Implement the `AcceptVisitor` method. The implementation will always be the same for any `Message` inheritor:
``` java
@Override
public void acceptVisitor(MessageVisitor<?> messageVisitor) { messageVisitor.visit(this); }
```

This method is called in the `MessageSerializer`. It leverages the visitor pattern and calls the correct visit method for the message type via double dispatch.

### 4. Update the MessageVisitor Interface
In the interface, add the visit method for the new message type:
``` java
public interface MessageVisitor<T> {
    T visit(Event e);
    T visit(PetriNet pn);
    // ...
    T visit(MYMESSAGE myMessage);
}
```

### 5. Implement the Visit Method in MessageSerializer
Implement the visit method in the `MessageSerializer` class:
``` java
@Override
public String visit(MYMESSAGE myMessage) {
    this.serialization = myMessage.getName() + ':' + serializePayload();
    return getSerialization();
}
```

You must define the `serializePayload()` method in your implementation. It should convert the `MYMESSAGE` instance into a string. This string will then be used by the `DeserializationStrategy` (in the next step) to reconstruct the `MYMESSAGE` instance.

### 6. Define a DeserializationStrategy
Create a `DeserializationStrategy` implementation for the `MYMESSAGE` type. The implementation **MUST** be defined exactly in the `communication.message.serialization.deserialization.impl` package. The current convention is to name the strategy `MYMESSAGEDeserializationStrategy`.
The classes in the `impl` package are used to automatically populate the `MessageFactory` class's static map at runtime, mapping message names to `DeserializationStrategy` instances.

Override the `deserialize(String)` method in the `DeserializationStrategy` implementation class you just created:
``` java
package communication.message.serialization.deserialization.impl;
import communication.message.serialization.deserialization.DeserializationStrategy;

public class MYMESSAGEDeserializationStrategy implements DeserializationStrategy {
    @Override
    public Message deserialize(String serializedPayload) { 
        return myDeserializationImplementation(serializedPayload);
    }
}
```

The input to this method is the payload of the string returned by the `MessageSerializer`. Note that the input does not contain the "`myMessage.getName() + ':'`" prefix from the `MessageSerializer.visit()`; that prefix was used and discarded by the `MessageFactory` to call the correct `DeserializationStrategy`.

You must define the `myDeserializationImplementation(String)` method in your implementation. It should convert the string into a `MYMESSAGE` instance.

### 7. Add the DeserializationStrategyRegistration Annotation
Add the `@DeserializationStrategyRegistration` annotation to the `MYMESSAGE` class, and set its `strategy` parameter to the `DeserializationStrategy` class you created above:
``` java
import annotations.AutoRegisterMessage;
import communication.message.serialization.deserialization.DeserializationStrategyRegistration;

@AutoRegisterMessage
@DeserializationStrategyRegistration(strategy = MYMESSAGEDeserializationStrategy.class)
public class MYMESSAGE extends Message {
   // Class implementation
}
```
### 8. Update the Message Type Schema
Add the `MYMESSAGE` class name as a string to the enum in `src/resources/jsonschemas/message_type_schema.json`. It is important that the enum value capitalization exactly matches the class name:
``` json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://www.dapm.org/message_type_schema.json",
  "title": "Message Type",
  "type": "string",
  "enum": ["Event", "PetriNet", "MYMESSAGE"]
}
```
### 9. You're Done!
