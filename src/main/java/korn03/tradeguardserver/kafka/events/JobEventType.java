package korn03.tradeguardserver.kafka.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.val;

import java.util.Arrays;

public enum JobEventType {
    CREATED,
    PAUSED,
    RESUMED,
    STEP_DONE,
    CANCELED_ORDERS,
    STOPPED,
    FINISHED,
    @JsonEnumDefaultValue UNKNOWN;

    //todo maybe configure ObjectMapper bean/ ACCEPT_CASE_INSENSITIVE_ENUMS in Kafka
    @JsonCreator
    public static JobEventType fromString(String value) {
        if (value == null || value.isBlank()) {
            return UNKNOWN;
        }
        //todo maybe soomething more robust/global configuration
        String normalizedValue = value
                .trim()
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .replaceAll("[-]", "_")
                .toUpperCase(); // Convert to ENUM format

        return Arrays.stream(values())
                .filter(type -> type.name().equals(normalizedValue))
                .findFirst()
                .orElse(UNKNOWN);
    }

    @JsonValue
    public String toJson() {
        return name();
    }
}
