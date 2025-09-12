package testconfig;

import communication.API.HTTPClient;
import communication.ConsumerFactory;
import communication.ProducerFactory;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import pipeline.PipelineBuilder;
import repository.PEInstanceRepository;
import repository.PipelineRepository;
import repository.TemplateRepository;

@TestConfiguration
public class PipelineBuilderTestConfig {
    @Bean
    public PipelineBuilder pipelineBuilder(
            HTTPClient webClient,
            PipelineRepository pipelineRepository,
            TemplateRepository templateRepository,
            PEInstanceRepository peInstanceRepository,
            ConsumerFactory consumerFactory,
            ProducerFactory producerFactory
    ) {
        return new PipelineBuilder(
                webClient,
                pipelineRepository,
                templateRepository,
                peInstanceRepository,
                consumerFactory,
                producerFactory
        );
    }

    @Bean
    public HTTPClient httpClient() {
        return Mockito.mock(HTTPClient.class);
    }

    @Bean
    public PipelineRepository pipelineRepository() {
        return new PipelineRepository();
    }

    @Bean
    public TemplateRepository templateRepository() {
        return Mockito.mock(TemplateRepository.class);
    }

    @Bean
    public PEInstanceRepository peInstanceRepository() {
        return Mockito.mock(PEInstanceRepository.class);
    }

    @Bean
    public ConsumerFactory consumerFactory() {
        return Mockito.mock(ConsumerFactory.class);
    }

    @Bean
    public ProducerFactory producerFactory() {
        return Mockito.mock(ProducerFactory.class);
    }
}
