package candidate_validation;

import communication.message.Message;
import pipeline.processingelement.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ProcessingElementReference {
    private final String organizationID;
    private final String organizationHostURL;
    private final String templateID;
    private final List<Class<? extends Message>> inputs;
    private final Class<? extends Message> output;
    private final int instanceNumber;
    private final Configuration configuration;

    public ProcessingElementReference(String organizationID,
                                      String organizationHostURL,
                                      String templateID,
                                      List<Class<? extends Message>> inputs,
                                      Class<? extends Message> output,
                                      int instanceNumber,
                                      Configuration configuration) {
        // only output is allowed to be null
        assert organizationID != null && organizationHostURL != null && templateID != null && inputs != null && configuration != null: "stop being lazy.";
        assert !organizationID.isEmpty() && !templateID.isEmpty() : "indistinguishable orgID and templateID";
        assert instanceNumber > 0 : "instanceID must be positive integer by convention (CFG definition).";

        this.organizationID = organizationID;
        this.organizationHostURL = organizationHostURL;
        this.templateID = templateID;
        this.inputs = inputs;
        this.output = output;
        this.instanceNumber = instanceNumber;
        this.configuration = configuration;

        assert !(isSource() && isSink()) : "Processing elements must either have inputs or output or both.";
    }

    public String getOrganizationID() { return this.organizationID; }
    public String getOrganizationHostURL() { return this.organizationHostURL; }
    public String getTemplateID() { return this.templateID; }
    public int getInstanceNumber() { return this.instanceNumber; }

    /** returns a copy of the inputs */
    public List<Class<? extends Message>> getInputs() { return List.copyOf(inputs); }
    public Class<? extends Message> getOutput() { return output; }
    public Configuration getConfiguration() { return configuration; }

    public Class<? extends Message> typeAt(int index) { return inputs.get(index); }

    public int inputCount() { return inputs.size(); }

    public boolean isSource() {
        return inputs.isEmpty();
    }

    public boolean isSink() {
        return output == null;
    }

    public boolean isOperator() { return !isSource() && !isSink(); }

    @Override
    public String toString() {
        String inputsString = String.valueOf(inputs.size());
        String outputString = output == null ? "null" : output.getSimpleName();
        List<String> attributes = List.of(organizationID, organizationHostURL, templateID, inputsString, outputString, instanceNumber, configuration).stream().map(Object::toString).toList();
        return "MPE[" + String.join(", ", attributes) + "]";
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) { return true; }
        if (!(other instanceof ProcessingElementReference otherMPE)) { return false; }
        return organizationID.equals(otherMPE.organizationID)
                && templateID.equals(otherMPE.templateID)
                && organizationHostURL.equals(otherMPE.organizationHostURL)
                && inputs.equals(otherMPE.inputs)
                && Objects.equals(output, otherMPE.output) // Objects.equals handles nulls properly
                && instanceNumber == otherMPE.instanceNumber
                && configuration.equals(otherMPE.configuration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organizationID, organizationHostURL, templateID, inputs, output, instanceNumber, configuration);
    }
}
