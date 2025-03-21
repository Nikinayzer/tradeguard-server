package korn03.tradeguardserver.kafka.events;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static korn03.tradeguardserver.kafka.events.JobEventType.*;

public class JobEventTypeDeserializer extends JsonDeserializer<JobEventType> {

    @Override
    public JobEventType deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {

        // Read the 'event_type' content as a tree
        JsonNode node = jp.readValueAsTree();

        // 1) If it's a plain string => e.g. "Created", "Paused"
        if (node.isTextual()) {
            String text = node.asText();
            return switch (text) {
                case "Created"        -> new Created();
                case "CanceledOrders" -> new CanceledOrders();
                case "Paused"         -> new Paused();
                case "Resumed"        -> new Resumed();
                case "Stopped"        -> new Stopped();
                case "Finished"       -> new Finished();
                default -> throw new IllegalArgumentException(
                                "Unrecognized string variant: " + text);
            };
        }

        // 2) Otherwise, we expect an object with exactly one field:
        if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node;
            // e.g. "StepDone" -> 3
            // or "Error" -> "Some message"
            // or "OrdersPlaced" -> [...]
            if (obj.has("StepDone")) {
                int stepIndex = obj.get("StepDone").asInt();
                return new StepDone(stepIndex);
            }
            if (obj.has("Error")) {
                String errMsg = obj.get("Error").asText();
                return new ErrorEvent(errMsg);
            }
            if (obj.has("OrdersPlaced")) {
                // parse array of logs
                JsonNode arr = obj.get("OrdersPlaced");
                if (!arr.isArray()) {
                    throw new IllegalArgumentException("'OrdersPlaced' must be array");
                }
                List<OpenOrderLog> logs = new ArrayList<>();
                for (JsonNode item : arr) {
                    // We can let Jackson do the heavy lifting by 
                    // converting each item into an OpenOrderLog:
                    OpenOrderLog log = jp.getCodec().treeToValue(item, OpenOrderLog.class);
                    logs.add(log);
                }
                return new OrdersPlaced(logs);
            }

            // If we get here => unknown single-field object
            throw new IllegalArgumentException(
                "Unknown object variant in event_type: " + obj.toString());
        }

        // fallback
        throw new IllegalArgumentException(
            "Expected string or single-field object for event_type, got: " + node.toString());
    }
}