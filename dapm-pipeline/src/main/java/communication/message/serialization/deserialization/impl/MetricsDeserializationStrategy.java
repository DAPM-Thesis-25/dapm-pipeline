package communication.message.serialization.deserialization.impl;

import communication.message.Message;
import communication.message.impl.Metrics;
import communication.message.serialization.deserialization.DeserializationStrategy;

import java.util.ArrayList;
import java.util.List;

public class MetricsDeserializationStrategy implements DeserializationStrategy {
    @Override
    public Message deserialize(String payload) {
        String listContents = payload.substring(1, payload.length() - 1); // remove square brackets
        String[] numbers = listContents.split(",");

        List<Double> deserialization = new ArrayList<>();
        for (String num : numbers) {
            num = num.trim();
            if (num.isEmpty()) throw new IllegalArgumentException("Empty metric value in metrics deserialization.");
            deserialization.add(Double.parseDouble(num));
        }

        return new Metrics(deserialization);
    }
}
