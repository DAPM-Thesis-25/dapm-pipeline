package repository;

import org.springframework.stereotype.Repository;
import pipeline.processingelement.ProcessingElement;
import utils.IDGenerator;

import java.util.HashMap;
import java.util.Map;

@Repository
public class PEInstanceRepository {

    private final Map<String, ProcessingElement> instances = new HashMap<>();

    public String storeInstance(ProcessingElement instance) {
        String instanceID = IDGenerator.generateInstanceID();
        instances.put(instanceID, instance);
        return instanceID;
    }

    public void removeInstance(String instanceID) {
        instances.remove(instanceID);
    }

    public <T extends ProcessingElement> T getInstance(String instanceID) {
        return (T) instances.get(instanceID);
    }
}