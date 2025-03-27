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

    private Long jobId;
    private String eventType;
    private Instant timestamp;
    private String source;

    @Lob
    private String eventData; // JSON-ified data (like placed orders, or created metadata)

    // Optionally link it to a Job entity if you want bi-directional
    // @ManyToOne(fetch = FetchType.LAZY)
    // private Job job;
}