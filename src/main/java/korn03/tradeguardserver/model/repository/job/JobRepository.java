package korn03.tradeguardserver.model.repository.job;

import korn03.tradeguardserver.model.entity.job.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    Optional<Job> findByJobId(Long jobId);
    List<Job> findByUserId(Long userId);
}