package korn03.tradeguardserver.model.entity.service.notifications;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing the type of push notification.
 */
public enum NotificationType {
    INFO("info"),
    WARNING("warning");

    private final String value;

    NotificationType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
