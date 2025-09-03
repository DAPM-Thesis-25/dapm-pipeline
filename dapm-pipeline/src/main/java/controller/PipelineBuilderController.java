package controller;

import communication.API.request.PEInstanceRequest;
import communication.API.response.PEInstanceResponse;
import communication.ConsumerFactory;
import communication.ProducerFactory;
import communication.config.ConsumerConfig;
import communication.config.ProducerConfig;
import communication.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pipeline.processingelement.Sink;
import pipeline.processingelement.operator.Operator;
import pipeline.processingelement.source.Source;
import repository.PEInstanceRepository;
import repository.TemplateRepository;
import utils.IDGenerator;
import utils.JsonUtil;

@RestController
@RequestMapping("/pipelineBuilder")
public class PipelineBuilderController {
    @Value("${organization.broker.port}")
    private String brokerURL;

    private final ProducerFactory producerFactory;
    private final ConsumerFactory consumerFactory;
    private final TemplateRepository templateRepository;
    private final PEInstanceRepository peInstanceRepository;

    @Autowired
    public PipelineBuilderController(TemplateRepository templateRepository,
                                     PEInstanceRepository peInstanceRepository,
                                     ConsumerFactory consumerFactory,
                                     ProducerFactory producerFactory) {
        this.templateRepository = templateRepository;
        this.peInstanceRepository = peInstanceRepository;
        this.consumerFactory = consumerFactory;
        this.producerFactory = producerFactory;
    }

    @PostMapping("/source/templateID/{templateID}")
    public ResponseEntity<PEInstanceResponse> configureSource(@PathVariable("templateID") String templateID, @RequestBody PEInstanceRequest requestBody) {
        System.out.println("We received your request");
        String decodedTemplateID = JsonUtil.decode(templateID);
        Source<Message> source = templateRepository.createInstanceFromTemplate(
                decodedTemplateID,
                requestBody.getConfiguration());
        System.out.println(source);

        if (source != null) {
            String topic = IDGenerator.generateTopic();
            ProducerConfig producerConfig = new ProducerConfig(brokerURL, topic);
            System.out.println(producerConfig.brokerURL());
            producerFactory.registerProducer(source, producerConfig);
            String instanceID = peInstanceRepository.storeInstance(source);

            return ResponseEntity.ok(new PEInstanceResponse
                    .Builder(decodedTemplateID, instanceID)
                    .producerConfig(producerConfig)
                    .build());
        }
        return ResponseEntity.badRequest().body(null);
    }

    @PostMapping("/operator/templateID/{templateID}")
    public ResponseEntity<PEInstanceResponse> createOperator(@PathVariable("templateID") String templateID, @RequestBody PEInstanceRequest requestBody) {
        String decodedTemplateID = JsonUtil.decode(templateID);
        Operator<Message, Message> operator = templateRepository.createInstanceFromTemplate(
                decodedTemplateID,
                requestBody.getConfiguration());

        if (operator != null) {
            for (ConsumerConfig config : requestBody.getConsumerConfigs()) {
                consumerFactory.registerConsumer(operator, config);
            }
            String topic = IDGenerator.generateTopic();
            ProducerConfig producerConfig = new ProducerConfig(brokerURL, topic);
            producerFactory.registerProducer(operator, producerConfig);

            String instanceID = peInstanceRepository.storeInstance(operator);
            return ResponseEntity.ok(new PEInstanceResponse
                    .Builder(decodedTemplateID, instanceID)
                    .producerConfig(producerConfig)
                    .build());
        }
        return ResponseEntity.badRequest().body(null);
    }

    @PostMapping("/sink/templateID/{templateID}")
    public ResponseEntity<PEInstanceResponse> createSink(@PathVariable("templateID") String templateID, @RequestBody PEInstanceRequest requestBody) {
        String decodedTemplateID = JsonUtil.decode(templateID);
        Sink sink = templateRepository.createInstanceFromTemplate(
                decodedTemplateID,
                requestBody.getConfiguration());

        if (sink != null) {
            for (ConsumerConfig config : requestBody.getConsumerConfigs()) {
                consumerFactory.registerConsumer(sink, config);
            }
            String instanceID = peInstanceRepository.storeInstance(sink);
            return ResponseEntity.ok(new PEInstanceResponse
                    .Builder(decodedTemplateID, instanceID)
                    .build());
        }
        return ResponseEntity.badRequest().body(null);
    }
}
