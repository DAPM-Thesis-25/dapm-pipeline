package communication.message.serialization.deserialization;

import communication.message.Message;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.*;

public class MessageFactory {
    private static final HashMap<String, DeserializationStrategy> strategyMap = new HashMap<>();

    // Use reflection to retrieve deserialization strategies from Message classes' annotation, and use it to populate the strategyMap
    static {
        Set<Class<? extends Message>> classes = getAllNonAbstractMessageClasses();
        throwMissingAnnotationError(classes);
        registerClasses(classes);

    }

    /** Throws a runtime exception if some non-abstract Message class does not have @DeserializationStrategyRegistration. */
    private static void throwMissingAnnotationError(Set<Class<? extends Message>> nonAbstractMessageClasses) {
        for (Class<? extends Message> messageClass : nonAbstractMessageClasses) {
            if (!messageClass.isAnnotationPresent(DeserializationStrategyRegistration.class)) {
                throw new IllegalStateException("Message class inheritor " + messageClass.getName() + " does not have a DeserializationStrategyRegistration annotation. All non-abstract Message inheritors must have this annotation.");
            }
        }
    }

    private static void registerClasses(Set<Class<? extends Message>> classes) {
        for (Class<? extends Message> messageClass : classes) {
            DeserializationStrategyRegistration annotation = messageClass.getAnnotation(DeserializationStrategyRegistration.class);
            if (annotation == null) { throw new IllegalStateException("Message class inheritor " + messageClass.getName() + " does not have a DeserializationStrategyRegistration annotation. All non-abstract Message inheritors must have this annotation."); }
            try {
                DeserializationStrategy strategy = annotation.strategy().getDeclaredConstructor().newInstance();
                String name = messageClass.getName();
                strategyMap.put(name, strategy);
            } catch (Exception e) { throw new IllegalStateException("Could not instantiate " + messageClass.getName(), e); }
        }
    }

    public static Message deserialize(String serialization) {
        assert serialization != null && !serialization.isEmpty();

        String[] typeAndPayload = serialization.split(":", 2);
        assert typeAndPayload.length == 2 : "serialization pattern has changed from 'Message_subtype:payload'";

        String className = typeAndPayload[0];
        DeserializationStrategy strategy = strategyMap.get(className);
        assert strategy != null : "deserialization for " + className + " has not been added to the MessageFactory";

        return strategy.deserialize(typeAndPayload[1]);
    }

    /** Retrieves all non-abstract Message inheritors located in communication.message.impl. */
    private static Set<Class<? extends Message>> getAllNonAbstractMessageClasses() {
        String messagePackage = "communication.message.impl";
        Set<Class<? extends Message>> classes = new HashSet<>();
        try {
            Reflections reflections = new Reflections(messagePackage);
            Set<Class<? extends Message>> subTypes = reflections.getSubTypesOf(Message.class);
            for (Class<? extends Message> subType : subTypes) {
                if (!Modifier.isAbstract(subType.getModifiers())) { classes.add(subType); }
            }
        } catch (Exception e) {
            System.err.println("Unable to retrieve all message classes from package " + messagePackage + ": " + e.getMessage());
            e.printStackTrace();
        }
        return classes;
    }
}
