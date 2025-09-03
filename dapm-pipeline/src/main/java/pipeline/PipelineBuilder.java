package pipeline;


import candidate_validation.*;
import communication.API.*;
import communication.API.request.HTTPRequest;
import communication.API.request.PEInstanceRequest;
import communication.API.response.HTTPResponse;
import communication.API.response.PEInstanceResponse;
import communication.config.ConsumerConfig;
import communication.config.ProducerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import repository.PipelineRepository;
import utils.graph.DG;
import utils.JsonUtil;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class PipelineBuilder {
    private final HTTPClient webClient;
    private final PipelineRepository pipelineRepository;

    @Autowired
    public PipelineBuilder(HTTPClient webClient, PipelineRepository pipelineRepository) {
        this.webClient = webClient;
        this.pipelineRepository = pipelineRepository;
    }

    public void buildPipeline(String pipelineID, ValidatedPipeline validatedPipeline) {
        Pipeline pipeline = new Pipeline(pipelineID, validatedPipeline.getChannels());
        buildPipeline(pipeline);
        pipelineRepository.storePipeline(pipelineID, pipeline);
    }

    private void buildPipeline(Pipeline pipeline) {
        Map<ProcessingElementReference, PEInstanceResponse> configuredInstances = new HashMap<>();
        Set<ProcessingElementReference> currentLevel = pipeline.getSources();
        DG<ProcessingElementReference, Integer> directedGraph = pipeline.getDirectedGraph();

        while (!currentLevel.isEmpty()) {
            Set<ProcessingElementReference> nextLevel = new HashSet<>();
            for (ProcessingElementReference pe : currentLevel) {
                if (configuredInstances.containsKey(pe)) continue;
                // Check if all upstream connections are configuredInstances yet
                if (!allInputsReady(directedGraph, configuredInstances, pe)) {
                    // Defer to next level if waiting for inputs
                    nextLevel.add(pe);
                    continue;
                }

                if (pe.isSource()) {
                    String instanceID = createSource(configuredInstances, pe);
                    pipeline.addProcessingElement(instanceID, pe);
                } else if (pe.isOperator()) {
                    String instanceID = createOperator(directedGraph, configuredInstances, pe);
                    pipeline.addProcessingElement(instanceID, pe);
                } else if (pe.isSink()) {
                    String instanceID = createSink(directedGraph, configuredInstances, pe);
                    pipeline.addProcessingElement(instanceID, pe);
                }

                // Add all downstream nodes for the next level
                nextLevel.addAll(pipeline.getDirectedGraph().getDownStream(pe));
            }
            currentLevel = nextLevel;
        }
    }

    private boolean allInputsReady(DG<ProcessingElementReference, Integer> directedGraph, Map<ProcessingElementReference, PEInstanceResponse> configured, ProcessingElementReference pe) {
        return directedGraph.getUpstream(pe).stream().allMatch(configured::containsKey);
    }

    private List<ConsumerConfig> getConsumerConfigs(DG<ProcessingElementReference, Integer> directedGraph, ProcessingElementReference pe, Map<ProcessingElementReference, PEInstanceResponse> configured) {
        return directedGraph.getUpstream(pe).stream()
                .filter(node -> {
                    PEInstanceResponse response = configured.get(node);
                    ProducerConfig producerConfig = response != null ? response.getProducerConfig() : null;
                    boolean hasEdgeAttribute = directedGraph.getEdgeAttribute(node, pe) != null;
                    return producerConfig != null
                            && hasEdgeAttribute
                            && producerConfig.brokerURL() != null && !producerConfig.brokerURL().isEmpty()
                            && producerConfig.topic() != null && !producerConfig.topic().isEmpty();
                })
                .map(node -> new ConsumerConfig(
                        configured.get(node).getProducerConfig().brokerURL(),
                        configured.get(node).getProducerConfig().topic(),
                        directedGraph.getEdgeAttribute(node, pe)))
                .collect(Collectors.toList());
    }

    private String createSource(Map<ProcessingElementReference, PEInstanceResponse> configuredInstances, ProcessingElementReference pe) {
        PEInstanceResponse sourceResponse = sendCreateSourceRequest(pe);
        configuredInstances.put(pe, sourceResponse);
        return sourceResponse.getInstanceID();
    }

    private String createOperator(DG<ProcessingElementReference, Integer> directedGraph, Map<ProcessingElementReference, PEInstanceResponse> configuredInstances, ProcessingElementReference pe) {
        List<ConsumerConfig> consumerConfigs = getConsumerConfigs(directedGraph, pe, configuredInstances);
        if (consumerConfigs == null || consumerConfigs.isEmpty()) {
            throw new IllegalStateException("No ConsumerConfigs found for operator: " + pe);
        }
        PEInstanceResponse operatorResponse = sendCreateOperatorRequest(pe, consumerConfigs);
        configuredInstances.put(pe, operatorResponse);
        return operatorResponse.getInstanceID();
    }

    private String createSink(DG<ProcessingElementReference, Integer> directedGraph, Map<ProcessingElementReference, PEInstanceResponse> configuredInstances, ProcessingElementReference pe) {
        List<ConsumerConfig> consumerConfigs = getConsumerConfigs(directedGraph, pe, configuredInstances);
        if (consumerConfigs == null || consumerConfigs.isEmpty()) {
            throw new IllegalStateException("No ConsumerConfigs found for sink: " + pe);
        }
        PEInstanceResponse sinkResponse = sendCreateSinkRequest(pe, consumerConfigs);
        configuredInstances.put(pe, sinkResponse);
        return sinkResponse.getInstanceID();
    }

    private PEInstanceResponse sendCreateSourceRequest(ProcessingElementReference pe) {
        String encodedTemplateID = JsonUtil.encode(pe.getTemplateID());
        String url = pe.getOrganizationHostURL() + String.format(
                "/pipelineBuilder/source/templateID/%s",
                encodedTemplateID
        );
        PEInstanceRequest requestBody = new PEInstanceRequest();
        requestBody.setConfiguration(pe.getConfiguration());
        return sendPostRequest(url, requestBody);
    }

    private PEInstanceResponse sendCreateOperatorRequest(ProcessingElementReference pe, List<ConsumerConfig> consumerConfigs) {
        String encodedTemplateID = JsonUtil.encode(pe.getTemplateID());
        String url = pe.getOrganizationHostURL() + String.format(
                "/pipelineBuilder/operator/templateID/%s",
                encodedTemplateID
        );
        PEInstanceRequest requestBody = new PEInstanceRequest();
        requestBody.setConfiguration(pe.getConfiguration());
        requestBody.setConsumerConfigs(consumerConfigs);
        return sendPostRequest(url, requestBody);
    }

    private PEInstanceResponse sendCreateSinkRequest(ProcessingElementReference pe, List<ConsumerConfig> consumerConfigs) {
        String encodedTemplateID = JsonUtil.encode(pe.getTemplateID());
        String url = pe.getOrganizationHostURL() + String.format(
                "/pipelineBuilder/sink/templateID/%s",
                encodedTemplateID
        );
        PEInstanceRequest requestBody = new PEInstanceRequest();
        requestBody.setConfiguration(pe.getConfiguration());
        requestBody.setConsumerConfigs(consumerConfigs);
        return sendPostRequest(url, requestBody);
    }

    private PEInstanceResponse sendPostRequest(String url, PEInstanceRequest body) {
        HTTPResponse response = webClient.postSync(new HTTPRequest(url, JsonUtil.toJson(body)));

        if (response == null || response.body() == null) {
            throw new IllegalStateException("No response received from " + url);
        }
        PEInstanceResponse peInstanceResponse = JsonUtil.fromJson(response.body(), PEInstanceResponse.class);

        if (peInstanceResponse.getTemplateID() == null ||
                peInstanceResponse.getInstanceID() == null ||
                peInstanceResponse.getTemplateID().isEmpty() ||
                peInstanceResponse.getInstanceID().isEmpty()) {
            throw new IllegalStateException("Received invalid response from " + url + ": " + response.body());
        }
        return peInstanceResponse;
    }
}