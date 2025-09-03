# Running a pipeline
Run a pipeline starting from a pipeline assembly JSON representation in your organization.

## 1. Select a pipeline assembly JSON
Specify the path to your pipeline assembly JSON in your application. Update the file path in the main method.
```java
public static void main(String[] args) {
    String contents;
    try {
        contents = Files.readString(Paths.get("orgC/src/main/resources/simple_pipeline.json"));
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}
```
## 2. Get the JSON config schemas
Each processing element template requires a corresponding JSON config schema. Make sure all schemas needed by the pipeline are present in the given location.
```java
URI configURI = Paths.get("orgC/src/main/resources/config_schemas").toUri();
```

## 3. Create the validated pipeline
Transform the JSON into a `ValidatedPipeline` object. The constructor throws an error if the Pipeline cannot be realized. The pipeline cannot be realized if 1) The JSON contents do not follow the JSON schemas (in `src/main/resources/jsonschemas`), and configuration schemas of the templated, or 2) the given pipeline does not conform to the properties in `PipelineCandidateValidator`.

If successful, a `ValidatedPipeline` object is created.
```java
ValidatedPipeline validatedPipeline = new ValidatedPipeline(contents, configURI);
```

## 4. Build the pipeline
Validated pipelines can be built using the `PipelineBuilder`. It also takes a pipelineID as an argument.
```java
    public static void main(String[] args) {
.buildPipeline(pipelineID, validatedPipeline);
}    ConfigurableApplicationContext context = SpringApplication.run(OrgAApplication.class, args);
PipelineBuilder pipelineBuilder = context.getBean(PipelineBuilder.class);

String pipelineID = "PipelineExample";

pipelineBuilder
```

## 5. Execute the pipeline
Use the `pipelineID` as argument to execute the pipeline.
```java
    public static void main(String[] args) {
    ConfigurableApplicationContext context = SpringApplication.run(OrgAApplication.class, args);
    PipelineExecutionService executionService = context.getBean(PipelineExecutionService.class);

    String pipelineID = "PipelineExample";

    executionService.start(pipelineID);
}
```

## 6. Terminate the pipeline
Use the `pipelineID` as argument to terminate the pipeline.
```java
    public static void main(String[] args) {

    String pipelineID = "PipelineExample";

    executionService.terminate(pipelineID);
}
```