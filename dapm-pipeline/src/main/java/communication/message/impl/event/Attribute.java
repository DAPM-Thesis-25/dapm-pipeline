package communication.message.impl.event;

import java.util.Collections;
import java.util.Map;

public class Attribute<T> {
    private final String name;
    private final T value;
    private final Map<String, Attribute<?>> nestedAttributes;

    public Attribute(String name, T value) {
        this(name, value, Collections.emptyMap());
    }

    public Attribute(String name, T value, Map<String, Attribute<?>> nestedAttributes) {
        this.name = name;
        this.value = value;
        this.nestedAttributes = nestedAttributes == null
                ? Collections.emptyMap()
                : Map.copyOf(nestedAttributes);
    }

    public String getName() {return name;}
    public T getValue() {return value;}
    public Map<String, Attribute<?>> getNestedAttributes() {return nestedAttributes; }

    @Override
    public String toString() {
        // if the attribute is a string, wrap it in quotes
        String outputValue = (value instanceof String) ? "\"" + value + "\"" : value.toString();
        return "\"" + name + "\": " + outputValue;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof Attribute<?> otherAttribute)) return false;
        return name.equals(otherAttribute.name);
    }

    @Override
    public int hashCode() { return name.hashCode(); }
}
