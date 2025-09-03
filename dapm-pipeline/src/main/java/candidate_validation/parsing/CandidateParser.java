package candidate_validation.parsing;

import communication.message.Message;
import communication.message.MessageTypeRegistry;
import communication.message.serialization.parsing.JSONParser;
import candidate_validation.ChannelReference;
import candidate_validation.SubscriberReference;
import candidate_validation.ProcessingElementReference;
import pipeline.processingelement.Configuration;
import utils.Pair;

import java.net.URI;
import java.util.*;

public class CandidateParser implements Parser<Pair<Set<ProcessingElementReference>, Set<ChannelReference>>> {
    private final JsonSchemaValidator validator;

    public CandidateParser(URI configFolderPath) {
        this.validator = new JsonSchemaValidator(configFolderPath);
    }

    @Override
    public Pair<Set<ProcessingElementReference>, Set<ChannelReference>> deserialize(String json) throws JsonSchemaMismatch {
        // The JSON schema validator below will take care of throwing errors if the JSON is not correctly formatted
        // according to the pipeline_draft_json_schema.json. We can therefore omit throwing those errors afterward.
        validator.validatePipelineCandidate(json);

        Map<String, Object> jsonMap = (Map<String, Object>) (new JSONParser()).parse(json);

        List<Map<String, Object>> rawElements = (List<Map<String, Object>>) jsonMap.get("processing elements");
        Set<ProcessingElementReference> elements = getProcessingElementReferences(rawElements);

        List<Map<String, Object>> rawChannels = (List<Map<String, Object>>) jsonMap.get("channels");
        Set<ChannelReference> channels = getChannelReferences(rawChannels);

        for (Map<String, Object> elementMap : rawElements) {
            String configFilename = toFilenameWithoutExtension(
                    (String) elementMap.get("organizationID"),
                    (String) elementMap.get("templateID"))
                    + "_config_schema.json";

            String configJson = getConfigurationJSONString(elementMap);
            validator.validateConfiguration(configJson, configFilename);
        }

        return new Pair<>(elements, channels);
    }

    private String toFilenameWithoutExtension(String... subWords) {
        String illegalChars = "[\\\\/:*?\"<>|']";

        List<String> substrings = new ArrayList<>();
        for (String word : subWords) {
            String filename = word.replaceAll(illegalChars, "_");
            filename = filename.replaceAll("\\s+", "_");
            filename = filename.replaceAll("^_+|_+$", ""); // remove underscores from start/end
            substrings.add(filename);
        }

        return String.join("_", substrings).toLowerCase();
    }

    private String getConfigurationJSONString(Map<String, Object> elementMap) {
        Object configuration = elementMap.get("configuration");
        if (configuration == null) { throw new IllegalStateException("a processing element must have a configuration"); }
        return JSONParser.toJSONString(configuration);
    }

    private Set<ChannelReference> getChannelReferences(List<Map<String, Object>> rawChannels) {
        Set<ChannelReference> channels = new HashSet<>();
        for (Map<String, Object> rawChannel : rawChannels) {
            channels.add(getChannelReference(rawChannel));
        }
        return channels;
    }

    private ChannelReference getChannelReference(Map<String, Object> rawChannel) {
        ProcessingElementReference publisher = getProcessingElementReferences((Map<String, Object>) rawChannel.get("publisher"));
        List<Map<String, Object>> subscribersList = (List<Map<String, Object>>) rawChannel.get("subscribers");
        Set<SubscriberReference> subscribers = getSubscriberReferences(subscribersList);
        return new ChannelReference(publisher, subscribers);
    }

    private Set<SubscriberReference> getSubscriberReferences(List<Map<String, Object>> subscribersList) {
        Set<SubscriberReference> subscribers = new HashSet<>();
        for (Map<String, Object> rawSubscriber : subscribersList) {
            subscribers.add(getSubscriberReference(rawSubscriber));
        }
        return subscribers;
    }

    private SubscriberReference getSubscriberReference(Map<String, Object> rawSubscriber) {
        ProcessingElementReference element = getProcessingElementReferences((Map<String, Object>) rawSubscriber.get("processing element"));
        int portNumber = (int) rawSubscriber.get("portNumber");
        return new SubscriberReference(element, portNumber);
    }

    private Set<ProcessingElementReference> getProcessingElementReferences(List<Map<String, Object>> rawElements) {
        Set<ProcessingElementReference> elements = new HashSet<>();
        for (Map<String, Object> elementMap : rawElements) {
            elements.add(getProcessingElementReferences(elementMap));
        }
        return elements;
    }

    private ProcessingElementReference getProcessingElementReferences(Map<String, Object> elementMap) throws InvalidCandidate {
        String organizationID = (String) elementMap.get("organizationID");
        String organizationHostURL = (String) elementMap.get("hostURL");
        String templateID = (String) elementMap.get("templateID");
        List<Class<? extends Message>> inputs = extractInputs((List<String>) elementMap.get("inputs"));
        Class<? extends Message> output = extractOutput(elementMap);
        int instanceNumber = (int) elementMap.get("instanceNumber");
        Configuration configuration = new Configuration((Map<String, Object>) elementMap.get("configuration"));

        return new ProcessingElementReference(
                organizationID, organizationHostURL, templateID,inputs, output, instanceNumber, configuration);
    }

    private Class<? extends Message> extractOutput(Map<String, Object> elementMap) throws InvalidCandidate {
        String outputClassString = (String) elementMap.get("output");
        if (outputClassString == null)
        { return null; }
        return MessageTypeRegistry.getMessageType(outputClassString); //return MessageTypeRegistry.getMessageType(outputClassString);
    }

    private List<Class<? extends Message>> extractInputs(List<String> stringInputs) throws InvalidCandidate {
        List<Class<? extends Message>> messageClasses = new ArrayList<>();
        if (stringInputs.isEmpty())
        { return messageClasses; }
        for (String classString : stringInputs) {
            messageClasses.add(MessageTypeRegistry.getMessageType(classString));
        }
        return messageClasses;
    }
}