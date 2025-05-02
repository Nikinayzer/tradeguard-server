package korn03.tradeguardserver.kafka.events.jobUpdates;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static korn03.tradeguardserver.kafka.events.jobUpdates.JobEventType.*;

@Slf4j
public class JobEventTypeDeserializer extends JsonDeserializer<JobEventType> {

    @Override
    public JobEventType deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.readValueAsTree();

        // Case 1: If the node is a simple string like "Created", "Paused", etc.
        if (node.isTextual()) {
            String text = node.asText();
            return switch (text) {
                case "CanceledOrders" -> new CanceledOrders();
                case "Paused" -> new Paused();
                case "Resumed" -> new Resumed();
                case "Stopped" -> new Stopped();
                case "Finished" -> new Finished();
                default -> {
                    log.error("Unrecognized string variant for event_type: {}", text);
                    throw new IllegalArgumentException("Unrecognized string variant: " + text);
                }
            };
        }

        // Case 2: If the node is an object (e.g., { "StepDone": 3 })
        if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node;

            // Check for specific fields and deserialize accordingly
            if (obj.has("Created")) {
                JsonNode created = obj.get("Created");
                String name = created.get("name").asText();
                Long userId = Long.parseLong(created.get("user_id").asText());
                List<String> coins = extractCoins(created.get("coins"));
                String side = created.get("side").asText();
                Double discountPct = created.get("discount_pct").asDouble();
                Double amount = created.get("amount").asDouble();
                Integer stepsTotal = created.get("steps_total").asInt();
                Double durationMinutes = created.get("duration_minutes").asDouble();

                CreatedMeta createdEventData = new CreatedMeta(name, userId, coins, side, discountPct, amount, stepsTotal, durationMinutes);
                return new Created(createdEventData);
            }
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
                    log.error("'OrdersPlaced' should be an array, but was: {}", arr);
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
            log.error("Unknown object variant in event_type: {}", obj.toString());
            throw new IllegalArgumentException("Unknown object variant in event_type: " + obj.toString());
        }

        // Case 3: If neither string nor object, log and throw an error
        log.error("Expected string or object for event_type, got: {}", node.toString());
        throw new IllegalArgumentException("Expected string or object for event_type, got: " + node.toString());
    }

    private List<String> extractCoins(JsonNode coinsNode) {
        List<String> coins = new ArrayList<>();
        if (coinsNode.isArray()) {
            for (JsonNode coin : coinsNode) {
                coins.add(coin.asText());
            }
        }
        return coins;
    }
}
