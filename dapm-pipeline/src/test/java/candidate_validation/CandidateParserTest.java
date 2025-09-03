package candidate_validation;

import communication.message.Message;
import communication.message.impl.event.Event;
import communication.message.impl.petrinet.PetriNet;
import org.junit.jupiter.api.Test;
import pipeline.processingelement.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class CandidateParserTest {

    public static ValidatedPipeline getValidatedPipeline(String jsonPath) {
        String contents;
        try { contents = Files.readString(Paths.get(jsonPath)); }
        catch (IOException e) {
            System.out.println(System.getProperty("user.dir") + "\n\n");
            throw new RuntimeException(e);
        }
        URI configURI = Paths.get("src/test/resources/candidate_validation/template_config_schemas/").toAbsolutePath().toUri();
        return new ValidatedPipeline(contents, configURI);
    }

    public static ValidatedPipeline getSimpleValid() {
        String simpleValidPath = "src/test/resources/candidate_validation/simple_valid.json";

        String contents;
        try { contents = Files.readString(Paths.get(simpleValidPath) ); }
        catch (IOException e) {
            System.out.println(System.getProperty("user.dir") + "\n\n");
            throw new RuntimeException(e);
        }
        URI configURI = Paths.get("src/test/resources/candidate_validation/template_config_schemas/").toAbsolutePath().toUri();
        return new ValidatedPipeline(contents, configURI);
    }

    @Test
    public void schemaLoadable() {
        InputStream is = getClass().getClassLoader().getResourceAsStream("jsonschemas/pipeline_candidate_schema.json");
        assertNotNull(is);
    }

    @Test
    public void configSchemaLoadable() throws IOException {
        InputStream is = Paths.get("src/test/resources/candidate_validation/template_config_schemas/coca_cola_the_profit_miner_config_schema.json").toAbsolutePath().toUri().toURL().openStream();
        assertNotNull(is);
    }

    @Test
    public void validCandidate() {
        String path = "src/test/resources/candidate_validation/simple_valid.json";

        // make source
        List<Class<? extends Message>> sourceInputs = new ArrayList<>();
        Class<? extends Message> sourceOutput = Event.class;
        ProcessingElementReference source = new ProcessingElementReference("Pepsi", "http://localhost:8082", "$$$ Source", sourceInputs, sourceOutput, 1, new Configuration(new HashMap<>()));

        // make operator
        List<Class<? extends Message>> operatorInputs = List.of(Event.class);
        Class<? extends Message> operatorOutput = PetriNet.class;
        ProcessingElementReference operator = new ProcessingElementReference("Coca Cola", "http://localhost:8092","The Profit Miner", operatorInputs, operatorOutput, 1, new Configuration(new HashMap<>()));

        // make sink
        List<Class<? extends Message>> sinkInputs = List.of(PetriNet.class);
        Class<? extends Message> sinkOutput = null;
        ProcessingElementReference sink = new ProcessingElementReference("DTU", "http://localhost:8102", "Dream Sink", sinkInputs, sinkOutput, 1, new Configuration(new HashMap<>()) );

        Set<ProcessingElementReference> expectedElements = Set.of(source, operator, sink);

        SubscriberReference operatorPort1 = new SubscriberReference(operator, 1);
        SubscriberReference sinkPort1 = new SubscriberReference(sink, 1);
        ChannelReference c1 = new ChannelReference(source, operatorPort1);
        ChannelReference c2 = new ChannelReference(operator, sinkPort1);
        Set<ChannelReference> expectedChannels = Set.of(c1, c2);

        ValidatedPipeline outputPipeline = getValidatedPipeline(path);
        Set<ProcessingElementReference> outputElements = outputPipeline.getElements();
        Set<ChannelReference> outputChannels = outputPipeline.getChannels();

        assertEquals(expectedElements, outputElements);
        assertEquals(expectedChannels, outputChannels);
    }

    @Test
    public void elementOrderInvariance() {
        String outputPath = "src/test/resources/candidate_validation/parser/element_order_invariance.json";
        ValidatedPipeline output = getValidatedPipeline(outputPath);
        ValidatedPipeline expected = getSimpleValid();
        assertEquals(output, expected);
    }

    @Test
    public void channelOrderInvariance() {
        String outputPath = "src/test/resources/candidate_validation/parser/channel_order_invariance.json";
        ValidatedPipeline output = getValidatedPipeline(outputPath);
        ValidatedPipeline expected = getSimpleValid();
        assertEquals(output, expected);
    }

    @Test
    public void duplicate() {
        // It should not matter whether a channel or element exists twice [with same instanceID] in the given json
        String outputPath = "src/test/resources/candidate_validation/parser/duplicate.json";
        assertThrows(RuntimeException.class, () -> getValidatedPipeline(outputPath));
    }

    @Test
    public void empty() {
        String path = "src/test/resources/candidate_validation/parser/empty.json";
        assertThrows(RuntimeException.class, () -> {
            CandidateParserTest.getValidatedPipeline(path);
        });
    }

    @Test
    public void singleElement() {
        String path = "src/test/resources/candidate_validation/parser/single_element.json";
        assertThrows(RuntimeException.class, () -> {
            CandidateParserTest.getValidatedPipeline(path);
        });
    }

    @Test
    public void noChannels() {
        String path = "src/test/resources/candidate_validation/parser/no_channels.json";
        assertThrows(RuntimeException.class, () -> {
            CandidateParserTest.getValidatedPipeline(path);
        });
    }

    @Test
    public void nullInputs() {
        // A source must be represented by an empty array (by convention) - not by null.
        String path = "src/test/resources/candidate_validation/parser/null_inputs.json";
        assertThrows(RuntimeException.class, () -> {
            CandidateParserTest.getValidatedPipeline(path);
        });
    }

    @Test
    public void configurationParsing() {
        String path = "src/test/resources/candidate_validation/parser/configuration_parsing.json";

        Map<String, Object> map = new HashMap<>();
        map.put("number", 0.5);
        map.put("string", "some string");
        map.put("optional config", "not required");
        Configuration expectedConfiguration = new Configuration(map);
        ProcessingElementReference expected = new ProcessingElementReference(
                "Pepsi",
                "http://localhost:8082",
                "CONFIGURATION",
                new ArrayList<>(),
                Event.class,
                1,
                expectedConfiguration);

        ValidatedPipeline candidate = CandidateParserTest.getValidatedPipeline(path);
        ProcessingElementReference output = candidate.getElements().stream()
                .filter(e -> e.getOrganizationID().equals("Pepsi"))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("parsing of Pepsi template failed."));

        assertEquals(output, expected);
    }

    @Test
    public void omitOptionalConfiguration() {
        String path = "src/test/resources/candidate_validation/parser/omit_optional_configuration.json";

        Map<String, Object> map = new HashMap<>();
        map.put("number", 0.5);
        map.put("string", "some string");
        Configuration expectedConfiguration = new Configuration(map);
        ProcessingElementReference expected = new ProcessingElementReference(
                "Pepsi",
                "http://localhost:8082",
                "CONFIGURATION",
                new ArrayList<>(),
                Event.class,
                1,
                expectedConfiguration);

        ValidatedPipeline candidate = CandidateParserTest.getValidatedPipeline(path);
        ProcessingElementReference output = candidate.getElements().stream()
                .filter(e -> e.getOrganizationID().equals("Pepsi"))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("parsing of Pepsi template failed."));

        assertEquals(expected, output);
    }

    @Test
    public void missingConfigurationProperty() {
        String path = "src/test/resources/candidate_validation/parser/missing_configuration_property.json";
        assertThrows(RuntimeException.class, () -> {
            CandidateParserTest.getValidatedPipeline(path);
        });
    }

    @Test
    public void undeclaredConfigurationProperty() {
        String path = "src/test/resources/candidate_validation/parser/undeclared_configuration_property.json";
        assertThrows(RuntimeException.class, () -> {
            CandidateParserTest.getValidatedPipeline(path);
        });
    }

    @Test
    public void noSource() {
        String path = "src/test/resources/candidate_validation/no_sink.json";
        assertThrows(RuntimeException.class, () -> {
            CandidateParserTest.getValidatedPipeline(path);
        });
    }

    @Test
    public void noSink() {
        String path = "src/test/resources/candidate_validation/parser/no_sink.json";
        assertThrows(RuntimeException.class, () -> {
            CandidateParserTest.getValidatedPipeline(path);
        });
    }

    @Test
    public void producingSink() {
        // a sink which is the from element of a channel in the pipeline candidate; a sink should always be the to-element
        String path = "src/test/resources/candidate_validation/parser/producing_sink.json";
        assertThrows(RuntimeException.class, () -> {
            CandidateParserTest.getValidatedPipeline(path);
        });
    }

    @Test
    public void consumingSource() {
        // a sink which is the from element of a channel in the pipeline candidate; a sink should always be the to-element
        String path = "src/test/resources/candidate_validation/parser/consuming_source.json";
        assertThrows(RuntimeException.class, () -> {
            CandidateParserTest.getValidatedPipeline(path);
        });
    }

    @Test
    public void noConfiguration() {
        String path = "src/test/resources/candidate_validation/no_configuration.json";
        assertThrows(RuntimeException.class, () -> {
            CandidateParserTest.getValidatedPipeline(path);
        });
    }

    @Test void nullConfiguration() {
        String path = "src/test/resources/candidate_validation/null_configuration.json";
        assertThrows(RuntimeException.class, () -> {
            CandidateParserTest.getValidatedPipeline(path);
        });
    }

}
