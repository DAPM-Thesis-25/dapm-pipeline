package pipeline;

import candidate_validation.ChannelReference;
import candidate_validation.ProcessingElementReference;
import candidate_validation.SubscriberReference;
import utils.graph.DG;

import java.util.*;
import java.util.stream.Collectors;

public class Pipeline {
    private final String pipelineID;
    private final Map<String, ProcessingElementReference> processingElements;
    private DG<ProcessingElementReference, Integer> directedGraph;

    public Pipeline(String pipelineID, Set<ChannelReference> channelReferences) {
        processingElements = new HashMap<>();
        this.pipelineID = pipelineID;
        initializeDG(channelReferences);
    }

    public String getPipelineID() {return pipelineID; }

    // return an unmodifiable map to ensure that processing elements are only added via addProcessingElement()
    public Map<String, ProcessingElementReference> getProcessingElements() {return Collections.unmodifiableMap(processingElements);}

    public void addProcessingElement(String instanceID, ProcessingElementReference processingElementReference) {
        processingElements.put(instanceID, processingElementReference);
    }

    private void initializeDG(Set<ChannelReference> channelReferences) {
        directedGraph = new DG<>();
        for (ChannelReference cr : channelReferences) {
            ProcessingElementReference producer = cr.getPublisher();
            for (SubscriberReference sr : cr.getSubscribers()) {
                ProcessingElementReference consumer = sr.getElement();
                directedGraph.addEdgeWithAttribute(producer, consumer, sr.getPortNumber());
            }
        }
    }

    public DG<ProcessingElementReference, Integer> getDirectedGraph() {
        return directedGraph;
    }

    public String getInstanceID(ProcessingElementReference ref) {
        return processingElements.entrySet().stream()
                .filter(entry -> entry.getValue().equals(ref))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    public Set<ProcessingElementReference> getSinks() {
        return directedGraph.getNodes()
                .stream()
                .filter(ProcessingElementReference::isSink)
                .collect(Collectors.toSet());
    }

    public Set<ProcessingElementReference> getSources() {
        return directedGraph.getNodes()
                .stream()
                .filter(ProcessingElementReference::isSource)
                .collect(Collectors.toSet());
    }
}
