package korn03.tradeguardserver.model.entity.job;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "strategy", nullable = false)
    private JobStrategyType strategy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private JobStatusType status;

    @Column(name = "steps_done", nullable = false)
    private Integer stepsDone;

    @ElementCollection
    @CollectionTable(name = "job_coins", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "coins")
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

    //todo add proportion/force/randomness

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}
