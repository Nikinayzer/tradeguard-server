package korn03.tradeguardserver.model.entity.job;

import jakarta.persistence.*;
import korn03.tradeguardserver.kafka.events.JobEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "job_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Convert(converter = JobEventTypeJsonConverter.class)
    @Column(name = "event_type", nullable = false)
    private JobEventType eventType;

    @Column(name = "steps_done", nullable = false)
    private Integer stepsDone;

    @Column(name = "duration_minutes", nullable = false)
    private Double durationMinutes;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;
}
