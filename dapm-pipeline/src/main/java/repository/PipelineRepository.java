package repository;

import org.springframework.stereotype.Repository;
import pipeline.Pipeline;

import java.util.HashMap;
import java.util.Map;

@Repository
public class PipelineRepository {
    private final Map<String, Pipeline> pipelines = new HashMap<>();

    public void storePipeline(String pipelineID, Pipeline pipeline) {
        pipelines.put(pipelineID, pipeline);
    }

    public Pipeline getPipeline(String pipelineID) {
        return pipelines.get(pipelineID);
    }

    public void removePipeline(String pipelineID) {
        pipelines.remove(pipelineID);
    }
}
