package korn03.tradeguardserver.kafka.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents different types of job events in the system.
 */
@Getter
@AllArgsConstructor
public class JobEventType {
    private final JobEventTypeEnum type;

    /**
     * Factory method for creating an JobEventType instance.
     */
    public static JobEventType of(String eventType) {
        return new JobEventType(JobEventTypeEnum.fromString(eventType));
    }

    /**
     * JSON Deserialization Handling.
     */
    @JsonCreator
    public static JobEventType create(String value) {
        return of(value);
    }

    /**
     * JSON Serialization Handling.
     */
    @JsonValue
    public String toJson() {
        return type.name();
    }

    @Override
    public String toString() {
        return type.name();
    }

    /**
     * Possible event types. Unknown is used as a fallback to not crush the runtime.
     */
    public enum JobEventTypeEnum {
        CREATED,
        PAUSED,
        RESUMED,
        STEP_DONE,
        CANCELED_ORDERS,
        STOPPED,
        FINISHED,
        UNKNOWN;

        @JsonCreator
        public static JobEventTypeEnum fromString(String value) {
            for (JobEventTypeEnum type : values()) {
                if (toCamelCase(type.name()).equalsIgnoreCase(value)) {
                    return type;
                }
            }
            return UNKNOWN;
        }

        /**
         * Converts an enum name to camel case.
         * e.g.  STEP_DONE -> stepDone
         * @param enumName The enum name to convert
         * @return The enum name in camel case
         */
        private static String toCamelCase(String enumName) {
            String[] parts = enumName.toLowerCase().split("_");
            StringBuilder camelCaseName = new StringBuilder(parts[0]);
            for (int i = 1; i < parts.length; i++) {
                camelCaseName.append(Character.toUpperCase(parts[i].charAt(0))).append(parts[i].substring(1));
            }
            return camelCaseName.toString();
        }
    }
}
