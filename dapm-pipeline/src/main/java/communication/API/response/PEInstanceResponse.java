package communication.API.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import communication.config.ProducerConfig;


public class PEInstanceResponse {
    private final String templateID;
    private final String instanceID;
    private final ProducerConfig producerConfig;

    @JsonCreator
    public PEInstanceResponse(
            @JsonProperty("templateID") String templateID,
            @JsonProperty("instanceID") String instanceID,
            @JsonProperty("producerConfig") ProducerConfig producerConfig
            ) {
        this.templateID = templateID;
        this.instanceID = instanceID;
        this.producerConfig = producerConfig;
    }

    public static class Builder {
        private final String templateID;
        private final String instanceID;
        private ProducerConfig producerConfig;

        public Builder(String templateID, String instanceID) {
            this.templateID = templateID;
            this.instanceID = instanceID;
        }

        public Builder producerConfig(ProducerConfig config) {
            this.producerConfig = config;
            return this;
        }

        public PEInstanceResponse build() {
            return new PEInstanceResponse(templateID, instanceID, producerConfig);
        }
    }

    public String getTemplateID() {
        return templateID;
    }

    public ProducerConfig getProducerConfig() {return producerConfig;}

    public String getInstanceID() {
        return instanceID;
    }
}
