package korn03.tradeguardserver.model.entity.service.notifications;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing the category of push notification.
 */

public enum NotificationCategory {
    SYSTEM("system"),
    MARKETING("marketing"),
    USER("user"),
    JOB("job"),
    MARKET("market"),
    HEALTH("health");

    private final String value;

    NotificationCategory(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
