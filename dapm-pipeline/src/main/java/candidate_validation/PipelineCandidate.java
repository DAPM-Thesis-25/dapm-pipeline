package candidate_validation;

import candidate_validation.parsing.CandidateParser;
import candidate_validation.parsing.JsonSchemaMismatch;
import utils.Pair;

import java.net.URI;
import java.util.Objects;
import java.util.Set;

public class PipelineCandidate {

    private final Set<ProcessingElementReference> elements;
    private final Set<ChannelReference> channels;

    /**
     * @param json              the JSON representation of a pipeline candidate
     * @param configFolderPath  the path to the configuration schema folder. this folder should contain a schema
     *                          for each processing element in 'json' which specifies the configurations and allowed
     *                          values for said processing elements.
     * @throws RuntimeException if parsing fails or json does not conform to the pipeline candidate and configuration
     *                          schemas.
     */
    PipelineCandidate(String json, URI configFolderPath) throws JsonSchemaMismatch {
        Pair<Set<ProcessingElementReference>, Set<ChannelReference>> elementsAndChannels
                = (new CandidateParser(configFolderPath)).deserialize(json);
        this.elements = elementsAndChannels.first();
        this.channels = elementsAndChannels.second();
    }

    public Set<ProcessingElementReference> getElements() { return Set.copyOf(elements); }
    public Set<ChannelReference> getChannels() { return Set.copyOf(channels); }

    @Override
    public String toString() {
        return "PC[" +
                "elements=" + elements +
                ", channels=" + channels +
                ']';
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof PipelineCandidate otherPC)) return false;
        return elements.equals(otherPC.elements) && channels.equals(otherPC.channels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elements, channels);
    }
}
