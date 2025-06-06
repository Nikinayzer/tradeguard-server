package korn03.tradeguardserver.endpoints.dto.user.risk;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RiskCategory {
    OVERCONFIDENCE("overconfidence"),
    FOMO("fomo"),
    LOSS_BEHAVIOR("loss_behavior");

    private final String value;

    RiskCategory(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static RiskCategory fromValue(String value) {
        for (RiskCategory category : RiskCategory.values()) {
            if (category.value.equals(value)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown RiskCategory value: " + value);
    }
} 