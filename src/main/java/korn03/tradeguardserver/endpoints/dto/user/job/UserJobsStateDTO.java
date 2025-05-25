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
public class UserJobsStateDTO {
    private JobsSummary summary;
    private List<JobDTO> activeJobs;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobsSummary {
        private int totalJobsCount;
        private int activeJobsCount;
        private Instant lastUpdate;
    }
} 