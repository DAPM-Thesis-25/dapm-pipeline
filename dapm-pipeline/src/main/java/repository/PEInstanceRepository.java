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
        System.out.println("[PEInstanceRepository] Stored " + instance.getClass().getSimpleName()
                + " with ID = " + instanceID);        instances.put(instanceID, instance);
        return instanceID;
    }

    public void removeInstance(String instanceID) {
        instances.remove(instanceID);
    }

    public <T extends ProcessingElement> T getInstance(String instanceID) {
        ProcessingElement pe = this.instances.get(instanceID);
        System.out.println("[PEInstanceRepository] Lookup ID = " + instanceID
                + " -> Found? " + (pe != null));
        return (T) instances.get(instanceID);
    }
}