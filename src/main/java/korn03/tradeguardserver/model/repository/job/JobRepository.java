package korn03.tradeguardserver.model.repository.job;

import korn03.tradeguardserver.model.entity.job.Job;
import korn03.tradeguardserver.model.entity.job.JobStatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    Optional<Job> findByJobId(Long jobId);
    List<Job> findByUserId(Long userId);
    List<Job> findByUserIdAndCreatedAtAfter(Long userId, Instant createdAt);
    List<Job> findByUserIdAndStatusNotLike(Long userId, JobStatusType status);
}