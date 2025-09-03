

import candidate_validation.ValidatedPipeline;
import communication.API.HTTPClient;
import communication.API.request.HTTPRequest;
import communication.API.response.HTTPResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pipeline.Pipeline;
import pipeline.PipelineBuilder;
import repository.PipelineRepository;
import testconfig.PipelineBuilderTestConfig;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PipelineBuilderTestConfig.class})
public class PipelineBuilderTest {
    @Autowired
    private PipelineBuilder pipelineBuilder;
    @Autowired
    private HTTPClient httpClient;
    @Autowired
    private PipelineRepository pipelineRepository;

    @BeforeEach
    public void setUp() {
        Mockito.reset(httpClient);
    }

    public static ValidatedPipeline getSimpleValid() {
        String simpleValidPath = "src/test/resources/candidate_validation/simple_valid.json";

        String contents;
        try {
            contents = Files.readString(Paths.get(simpleValidPath));
        } catch (IOException e) {
            System.out.println(System.getProperty("user.dir") + "\n\n");
            throw new RuntimeException(e);
        }
        URI configURI = Paths.get("src/test/resources/candidate_validation/template_config_schemas/").toAbsolutePath().toUri();
        return new ValidatedPipeline(contents, configURI);
    }

    private void setUpMockResponses(Function<String, String> bodyForUrl) {
        Mockito.when(httpClient.postSync(Mockito.any(HTTPRequest.class)))
                .thenAnswer(invocation -> {
                    HTTPRequest request = invocation.getArgument(0, HTTPRequest.class);
                    String url = request.getUrl();
                    HTTPResponse mockResponse = Mockito.mock(HTTPResponse.class);
                    Mockito.when(mockResponse.body()).thenReturn(bodyForUrl.apply(url));
                    return mockResponse;
                });
    }

    private String getResponseFromFile(String path) {
        try (InputStream is = new java.io.FileInputStream(path)) {
            return new String(is.readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException("Failed to read response from file: " + path, e);
        }
    }

    void setUpSuccessfulMockResponses() {
        setUpMockResponses(url -> {
            if (url.contains("/source")) {
                return getResponseFromFile("src/test/resources/pipeline_builder/success/valid_source_response.json");
            } else if (url.contains("/operator")) {
                return getResponseFromFile("src/test/resources/pipeline_builder/success/valid_operator_response.json");
            } else if (url.contains("/sink")) {
                return getResponseFromFile("src/test/resources/pipeline_builder/success/valid_sink_response.json");
            }
            return null;
        });
    }

    private void setUpFailedMockResponsesNullTemplateId() {
        setUpMockResponses(url -> getResponseFromFile("src/test/resources/pipeline_builder/fail/invalid_source_response_null_templateID.json"));
    }

    void setUpFailedMockResponsesNullInstanceID() {
        setUpMockResponses(url -> getResponseFromFile("src/test/resources/pipeline_builder/fail/invalid_source_response_null_instanceID.json"));
    }

    private void setUpFailedMockResponsesNullProducerConfig() {
        setUpMockResponses(url -> getResponseFromFile("src/test/resources/pipeline_builder/fail/invalid_source_response_null_producerConfig.json"));
    }

    private void setUpFailedMockResponsesNullProducerConfigs() {
        setUpMockResponses(url -> getResponseFromFile("src/test/resources/pipeline_builder/fail/invalid_source_response_null_producerConfigs.json"));
    }


    void setUpFailedMockResponsesEmptyTemplateID() {
        setUpMockResponses(url -> getResponseFromFile("src/test/resources/pipeline_builder/fail/invalid_source_response_null_templateID.json"));
    }

    void setUpFailedMockResponsesEmptyInstanceID() {
        setUpMockResponses(url -> getResponseFromFile("src/test/resources/pipeline_builder/fail/invalid_source_response_empty_instanceID.json"));
    }

    void setUpFailedMockResponsesEmptyProducerConfigs() {
        setUpMockResponses(url -> getResponseFromFile("src/test/resources/pipeline_builder/fail/invalid_source_response_empty_producerConfig.json"));
    }

    private void setUpFailedMockResponsesNullResponseBody() {
        setUpMockResponses(url -> null);
    }


    void setUpFailedMockResponsesNullResponse() {
        Mockito.when(httpClient.postSync(Mockito.any(HTTPRequest.class)))
                .thenReturn(null);
    }

    @Test
    public void success() {
        setUpSuccessfulMockResponses();
        String pipelineID = "pipeline1";
        ValidatedPipeline validatedPipeline = getSimpleValid();

        pipelineBuilder.buildPipeline(pipelineID, validatedPipeline);
        Pipeline pipeline = pipelineRepository.getPipeline(pipelineID);

        assertNotNull(pipeline);
        assertEquals(pipelineID, pipeline.getPipelineID());
        assertEquals(3, pipeline.getProcessingElements().size());
        String instanceIDSource = "source-id";
        assertNotNull(pipeline.getProcessingElements().get(instanceIDSource));
        String instanceIDOperator = "operator-id";
        assertNotNull(pipeline.getProcessingElements().get(instanceIDOperator));
        String instanceIDSink = "sink-id";
        assertNotNull(pipeline.getProcessingElements().get(instanceIDSink));
    }

    @Test
    public void fail_null_template_id() {
        setUpFailedMockResponsesNullTemplateId();
        String pipelineID = "pipeline1";
        ValidatedPipeline validatedPipeline = getSimpleValid();
        assertThrows(
                IllegalStateException.class,
                () -> pipelineBuilder.buildPipeline(pipelineID, validatedPipeline)
        );
    }

    @Test
    public void fail_null_instance_id() {
        setUpFailedMockResponsesNullInstanceID();
        String pipelineID = "pipeline1";
        ValidatedPipeline validatedPipeline = getSimpleValid();

        assertThrows(
                IllegalStateException.class,
                () -> pipelineBuilder.buildPipeline(pipelineID, validatedPipeline)
        );
    }

    @Test
    public void fail_null_producer_config() {
        setUpFailedMockResponsesNullProducerConfig();
        String pipelineID = "pipeline1";
        ValidatedPipeline validatedPipeline = getSimpleValid();

        assertThrows(
                IllegalStateException.class,
                () -> pipelineBuilder.buildPipeline(pipelineID, validatedPipeline)
        );
    }

    @Test
    public void fail_null_producer_configs() {
        setUpFailedMockResponsesNullProducerConfigs();
        String pipelineID = "pipeline1";
        ValidatedPipeline validatedPipeline = getSimpleValid();

        assertThrows(
                IllegalStateException.class,
                () -> pipelineBuilder.buildPipeline(pipelineID, validatedPipeline)
        );
    }


    @Test
    public void fail_empty_template_id() {
        setUpFailedMockResponsesEmptyTemplateID();
        String pipelineID = "pipeline1";
        ValidatedPipeline validatedPipeline = getSimpleValid();

        assertThrows(
                IllegalStateException.class,
                () -> pipelineBuilder.buildPipeline(pipelineID, validatedPipeline)
        );
    }

    @Test
    public void fail_empty_instance_id() {
        setUpFailedMockResponsesEmptyInstanceID();
        String pipelineID = "pipeline1";
        ValidatedPipeline validatedPipeline = getSimpleValid();

        assertThrows(
                IllegalStateException.class,
                () -> pipelineBuilder.buildPipeline(pipelineID, validatedPipeline)
        );
    }

    @Test
    public void fail_empty_producer_configs() {
        setUpFailedMockResponsesEmptyProducerConfigs();
        String pipelineID = "pipeline1";
        ValidatedPipeline validatedPipeline = getSimpleValid();

        assertThrows(
                IllegalStateException.class,
                () -> pipelineBuilder.buildPipeline(pipelineID, validatedPipeline)
        );
    }

    @Test
    public void fail_null_response_body() {
        setUpFailedMockResponsesNullResponseBody();
        String pipelineID = "pipeline1";
        ValidatedPipeline validatedPipeline = getSimpleValid();

        assertThrows(
                IllegalStateException.class,
                () -> pipelineBuilder.buildPipeline(pipelineID, validatedPipeline)
        );
    }

    @Test
    public void fail_null_response() {
        setUpFailedMockResponsesNullResponse();
        String pipelineID = "pipeline1";
        ValidatedPipeline validatedPipeline = getSimpleValid();

        assertThrows(
                IllegalStateException.class,
                () -> pipelineBuilder.buildPipeline(pipelineID, validatedPipeline)
        );
    }
}