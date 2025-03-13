package korn03.tradeguardserver.model.repository.job;

import korn03.tradeguardserver.model.entity.job.JobEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobEventRepository extends JpaRepository<JobEvent, Long> {
    List<JobEvent> findByJobIdOrderByTimestampAsc(Long jobId);
}