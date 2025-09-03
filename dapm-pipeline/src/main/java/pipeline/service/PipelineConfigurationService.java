package pipeline.service;

import communication.API.HTTPClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PipelineConfigurationService {
    private final HTTPClient webClient;

    @Autowired
    public PipelineConfigurationService(HTTPClient webClient) {
        this.webClient = webClient;
    }
}
