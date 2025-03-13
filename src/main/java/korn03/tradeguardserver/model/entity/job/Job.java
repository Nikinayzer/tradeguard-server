package korn03.tradeguardserver.model.entity.job;

import jakarta.persistence.*;
import korn03.tradeguardserver.kafka.events.JobEventMessage;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "jobs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false, unique = true)
    private Long jobId;

    @Column(name = "user_id", nullable = true)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private JobStatus status;

    @Column(name = "steps_done", nullable = false)
    private Integer stepsDone;

    @Column(name = "name", nullable = false)
    private String name;

    @ElementCollection
    @CollectionTable(name = "job_coins", joinColumns = @JoinColumn(name = "job_id")) //maybe change to something more convenient
    @Column(name = "coin")
    private List<String> coins;

    @Column(name = "side")
    private String side;

    @Column(name = "discount_pct")
    private Double discountPct;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "steps_total")
    private Integer stepsTotal;

    @Column(name = "duration_minutes")
    private Double durationMinutes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Converts a JobEventMessage into a Job for persistence.
     */
    public static Job fromJobEvent(JobEventMessage jobEventMessage) {
        return Job.builder()
                .jobId(jobEventMessage.getJobId())
                //todo add userId
                .status(JobStatus.fromEventType(jobEventMessage.getJobEventType().getType()))
                .stepsDone(jobEventMessage.getStepsDone())
                .name(jobEventMessage.getName())
                .coins(jobEventMessage.getCoins())
                .side(jobEventMessage.getSide())
                .discountPct(jobEventMessage.getDiscountPct())
                .amount(jobEventMessage.getAmount())
                .stepsTotal(jobEventMessage.getStepsTotal())
                .durationMinutes(jobEventMessage.getDurationMinutes())
                .createdAt(jobEventMessage.getTimestamp())
                .updatedAt(jobEventMessage.getTimestamp())
                .build();
    }
}
