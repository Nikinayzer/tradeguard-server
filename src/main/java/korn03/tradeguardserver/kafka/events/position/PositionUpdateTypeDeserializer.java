package korn03.tradeguardserver.kafka.events.position;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class PositionUpdateTypeDeserializer extends JsonDeserializer<PositionUpdateType> {

    @Override
    public PositionUpdateType deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        String value = jp.getValueAsString();
        try {
            return PositionUpdateType.valueOf(value);
        } catch (IllegalArgumentException e) {
            log.error("Unrecognized position update type: {}", value);
            throw new IllegalArgumentException("Unrecognized position update type: " + value);
        }
    }
} 