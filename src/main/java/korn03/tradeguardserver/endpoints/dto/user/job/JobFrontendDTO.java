package korn03.tradeguardserver.endpoints.dto.user.job;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobFrontendDTO {
    private Long jobId;
    private Long userId;
    private String strategy;
    private String status;
    private String side;
    private Double discountPct;
    private Integer stepsTotal;
    private Double durationMinutes;
    private Instant createdAt;
    private Instant updatedAt;
    private String source;
    private JobProgress progress;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobProgress {
        private Integer currentStep;
        private Double progressPct;
        private Instant estimatedCompletion;
    }
} 