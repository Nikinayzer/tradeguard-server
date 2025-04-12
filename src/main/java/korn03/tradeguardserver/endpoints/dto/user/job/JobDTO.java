package korn03.tradeguardserver.endpoints.dto.user.job;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobDTO {
    private Long id;
    private String strategy;
    private String side;
    private String status;
    private Double amount;
    private List<String> coins;
    private Double discountPct;
    private Integer stepsDone;
    private Integer stepsTotal;
    private Double durationMinutes;
    private Instant createdAt;
    private Instant updatedAt;
}
