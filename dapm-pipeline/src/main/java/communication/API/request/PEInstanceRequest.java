package communication.API.request;

import communication.config.ConsumerConfig;
import pipeline.processingelement.Configuration;

import java.util.List;

public class PEInstanceRequest {

    private List<ConsumerConfig> consumerConfigs;
    private Configuration configuration;
    private String token;

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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
