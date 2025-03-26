package korn03.tradeguardserver.kafka.events;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static korn03.tradeguardserver.kafka.events.JobEventType.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobEventTypeDeserializer extends JsonDeserializer<JobEventType> {

    private static final Logger logger = LoggerFactory.getLogger(JobEventTypeDeserializer.class);

    @Override
    public JobEventType deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.readValueAsTree();

        // Case 1: If the node is a simple string like "Created", "Paused", etc.
        if (node.isTextual()) {
            String text = node.asText();
            return switch (text) {
                case "Created" -> new Created();
                case "CanceledOrders" -> new CanceledOrders();
                case "Paused" -> new Paused();
                case "Resumed" -> new Resumed();
                case "Stopped" -> new Stopped();
                case "Finished" -> new Finished();
                default -> {
                    logger.error("Unrecognized string variant for event_type: {}", text);
                    throw new IllegalArgumentException("Unrecognized string variant: " + text);
                }
            };
        }

        // Case 2: If the node is an object (e.g., { "StepDone": 3 })
        if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node;

            // Check for specific fields and deserialize accordingly
            if (obj.has("StepDone")) {
                int stepIndex = obj.get("StepDone").asInt();
                return new StepDone(stepIndex);
            }
            if (obj.has("Error")) {
                String errMsg = obj.get("Error").asText();
                return new ErrorEvent(errMsg);
            }
            if (obj.has("OrdersPlaced")) {
                // Parse the array for "OrdersPlaced"
                JsonNode arr = obj.get("OrdersPlaced");
                if (!arr.isArray()) {
                    logger.error("'OrdersPlaced' should be an array, but was: {}", arr);
                    throw new IllegalArgumentException("'OrdersPlaced' must be an array");
                }
                List<OpenOrderLog> logs = new ArrayList<>();
                for (JsonNode item : arr) {
                    OpenOrderLog log = jp.getCodec().treeToValue(item, OpenOrderLog.class);
                    logs.add(log);
                }
                return new OrdersPlaced(logs);
            }

            // If none of the above fields are found, it's an unknown object type
            logger.error("Unknown object variant in event_type: {}", obj.toString());
            throw new IllegalArgumentException("Unknown object variant in event_type: " + obj.toString());
        }

        // Case 3: If neither string nor object, log and throw an error
        logger.error("Expected string or object for event_type, got: {}", node.toString());
        throw new IllegalArgumentException("Expected string or object for event_type, got: " + node.toString());
    }
}
