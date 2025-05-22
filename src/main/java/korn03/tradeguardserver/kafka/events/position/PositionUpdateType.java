package korn03.tradeguardserver.kafka.events.position;

public enum PositionUpdateType {
    Closed,
    Decreased,
    Increased,
    NoChange,
    Snapshot,
}
