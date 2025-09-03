package communication.message.serialization.deserialization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Used by the MessageFactory to register the non-abstract Message inheritors' deserialization strategy. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DeserializationStrategyRegistration {
    Class<? extends DeserializationStrategy> strategy();
}
