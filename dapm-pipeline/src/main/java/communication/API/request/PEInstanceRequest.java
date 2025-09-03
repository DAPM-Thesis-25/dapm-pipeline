package communication.API.request;

import communication.config.ConsumerConfig;
import pipeline.processingelement.Configuration;

import java.util.List;

public class PEInstanceRequest {

    private List<ConsumerConfig> consumerConfigs;
    private Configuration configuration;

    public PEInstanceRequest() {
    }

    public List<ConsumerConfig> getConsumerConfigs() {
        return consumerConfigs;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConsumerConfigs(List<ConsumerConfig> consumerConfigs) {
        this.consumerConfigs = consumerConfigs;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
