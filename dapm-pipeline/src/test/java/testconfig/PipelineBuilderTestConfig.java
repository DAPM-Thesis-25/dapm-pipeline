package testconfig;

import communication.API.HTTPClient;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import pipeline.PipelineBuilder;
import repository.PipelineRepository;

@TestConfiguration
public class PipelineBuilderTestConfig {
    @Bean
    public PipelineBuilder pipelineBuilder(HTTPClient httpClient, PipelineRepository pipelineRepository) {
        return new PipelineBuilder(httpClient, pipelineRepository);
    }

    @Bean
    public HTTPClient httpClient() {
        return Mockito.mock(HTTPClient.class);
    }

    @Bean
    public PipelineRepository pipelineRepository() { return new PipelineRepository(); }
}
