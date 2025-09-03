package pipeline.processingelement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.Map;

public class Configuration {
    private final Map<String, Object> configuration;

    @JsonCreator // annotations to properly deserialize using Jackson
    public Configuration(@JsonProperty("configuration") Map<String, Object> configuration) {
        this.configuration = configuration;
    }

    public Object get(String key) { return configuration.get(key); }

    public Map<String, Object> getConfiguration() {
        return Collections.unmodifiableMap(configuration);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        Configuration otherConfig = (Configuration) other;
        return configuration.equals(otherConfig.configuration);
    }

    @Override
    public int hashCode() { return configuration.hashCode(); }

}
