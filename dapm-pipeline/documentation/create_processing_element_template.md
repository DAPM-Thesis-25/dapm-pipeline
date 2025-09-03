# Create processing element template
Create and store processing element templates in your organization. These templates are of types Source, Operator, and Sink.

## Sources
Sources are responsible for producing messages to be processed downstream.

Extend the `SimpleSource`, `WebSource`, or `Source` abstract classes. As parameters it takes a message type, this should be the expected output of this source. Implement the required methods. 

The SimpleSource could either be generating data internally or implement another mechanism to fetch external data. 
```java
public class EventSource extends WebSource<Event> {
    @Override
    protected Flux<Event> process() {
        // ...
    }
}
```
A WebSource returns a reactive stream of data from web endpoints.

```java
public class SourceA extends SimpleSource<Event> {

    @Override
    protected Event process() {
        //  ...
    }
}
```

## Operators
Operators process messages and transform data.

Extend the `SimpleOperator`, `MiningOperator`, or `Operator`. The parameters are again the expected output type. Implement the required methods.
```java
public class EventOperatorB extends SimpleOperator<Event> { } 

public class HeuristicsMiner extends MiningOperator<PetriNet> {}
```
In both implementations you will have to specify the expected input types of the operator and which port it will expect to receive this input.
```java
@Override
protected Map<Class<? extends Message>, Integer> setConsumedInputs() {
    Map<Class<? extends Message>, Integer> map = new HashMap<>();
    map.put(Event.class, 1);
    return map;
}
```

What makes the MiningOperator different from the SimpleOperator is that it produces a result together with a boolean in a Pair. This is used when it is conditional whether the output should be published or not.

```java
@Override
protected boolean publishCondition(Pair<PetriNet, Boolean> petriNetBooleanPair) {
    return petriNetBooleanPair.second();
}
```

## Sink
Extend the `Sink` abstract class and implement the required methods. Similarly to the operator your have to specify all expected input types in `setConsumedInputs()`

## Store the templates
Templates are stored in the `TemplateRepository`. Currently this happens in the main method of an organization. Store a template with a unique name and with its class.
```java
    public static void main(String[] args) {
    ConfigurableApplicationContext context = SpringApplication.run(OrgAApplication.class, args);
    TemplateRepository templateRepository = context.getBean(TemplateRepository.class);

    templateRepository.storeTemplate("SimpleSource", SourceA.class);
    templateRepository.storeTemplate("SimpleSink", SinkA.class);
    templateRepository.storeTemplate("EventSource", EventSource.class);
    templateRepository.storeTemplate("PetriNetSink", PetriNetSink.class);
    templateRepository.storeTemplate("LanguageFilter", LanguageFilter.class);
}
```

