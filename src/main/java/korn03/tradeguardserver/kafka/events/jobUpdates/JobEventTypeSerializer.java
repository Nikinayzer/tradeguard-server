package korn03.tradeguardserver.kafka.events.jobUpdates;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

public class JobEventTypeSerializer extends JsonSerializer<JobEventType> {

    @Override
    public void serialize(JobEventType value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        ObjectMapper mapper = (ObjectMapper) gen.getCodec();

        switch (value) {
            case JobEventType.Created created -> {
                ObjectNode createdNode = mapper.valueToTree(created.meta());
                ObjectNode wrapper = mapper.createObjectNode();
                wrapper.set("Created", createdNode);
                mapper.writeTree(gen, wrapper);

            }
            case JobEventType.Paused paused -> gen.writeString("Paused");
            case JobEventType.Resumed resumed -> gen.writeString("Resumed");
            case JobEventType.Stopped stopped -> gen.writeString("Stopped");
            case JobEventType.Finished finished -> gen.writeString("Finished");
            case JobEventType.CanceledOrders canceledOrders -> gen.writeString("CanceledOrders");
            case JobEventType.StepDone stepDone -> {
                ObjectNode wrapper = mapper.createObjectNode();
                wrapper.put("StepDone", stepDone.stepIndex());
                mapper.writeTree(gen, wrapper);

            }
            case JobEventType.ErrorEvent error -> {
                ObjectNode wrapper = mapper.createObjectNode();
                wrapper.put("Error", error.message());
                mapper.writeTree(gen, wrapper);

            }
            case JobEventType.OrdersPlaced ordersPlaced -> {
                ObjectNode wrapper = mapper.createObjectNode();
                ArrayNode ordersArray = mapper.valueToTree(ordersPlaced.orders());
                wrapper.set("OrdersPlaced", ordersArray);
                mapper.writeTree(gen, wrapper);

            }
            case null, default ->
                    throw new IllegalArgumentException("❌ Unsupported JobEventType: " + value.getClass().getSimpleName());
        }
    }
}
