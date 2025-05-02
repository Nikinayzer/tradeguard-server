package korn03.tradeguardserver.kafka.events.jobUpdates;

// ---------------------------------------------------
// 2) The possible variants of event_type, 
//    which correspond to your Rust enum arms
// ---------------------------------------------------

import com.fasterxml.jackson.annotation.JsonProperty;

// We can use a sealed interface (Java 17+) or a plain interface:
public sealed interface JobEventType
        permits JobEventType.CanceledOrders, JobEventType.Created, JobEventType.ErrorEvent, JobEventType.Finished,
        JobEventType.OrdersPlaced, JobEventType.Paused, JobEventType.Resumed, JobEventType.StepDone,
        JobEventType.Stopped {
    // no members needed, just a marker interface

// For the “string-only” variants, we create trivial records or classes:

    record Created( CreatedMeta meta) implements JobEventType {
    }

    record CanceledOrders() implements JobEventType {
    }

    record Paused() implements JobEventType {
    }

    record Resumed() implements JobEventType {
    }

    record Stopped() implements JobEventType {
    }

    record Finished() implements JobEventType {
    }

    // Next, the StepDone variant has a numeric payload:
    record StepDone(int stepIndex) implements JobEventType {
    }

    // The Error variant has a string payload:
    record ErrorEvent(String message) implements JobEventType {
    }

    record CreatedMeta(
            @JsonProperty("name") String name,
            @JsonProperty("user_id") Long userId,
            @JsonProperty("coins") java.util.List<String> coins,
            @JsonProperty("side") String side,
            @JsonProperty("discount_pct") Double discountPct,
            @JsonProperty("amount") Double amount,
            @JsonProperty("steps_total") Integer stepsTotal,
            @JsonProperty("duration_minutes") Double durationMinutes
    ) {
    }

    // OrdersPlaced has an array of items.
    record OpenOrderLog(
            @JsonProperty("job_id") int jobId,
            @JsonProperty("account_name") String accountName,
            @JsonProperty("order_id") String orderId,
            @JsonProperty("user_id") String userId
    ) {
    }

    record OrdersPlaced(java.util.List<OpenOrderLog> orders) implements JobEventType {
    }
}