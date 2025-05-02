package korn03.tradeguardserver.service.job;

import korn03.tradeguardserver.exception.NotFoundException;
import korn03.tradeguardserver.kafka.events.jobUpdates.JobEventMessage;
import korn03.tradeguardserver.mapper.JobMapper;
import korn03.tradeguardserver.model.entity.job.Job;
import korn03.tradeguardserver.model.entity.job.JobEvent;
import korn03.tradeguardserver.model.entity.job.JobStatusType;
import korn03.tradeguardserver.model.repository.job.JobEventRepository;
import korn03.tradeguardserver.model.repository.job.JobRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final JobEventRepository jobEventRepository;
    private final JobMapper jobMapper;

    private final Logger logger = LoggerFactory.getLogger(JobService.class);

    /**
     * Handles a job event from Kafka.
     * Updates the Job entity if it exists; otherwise, creates a new Job entity.
     * Always creates and saves a JobEvent entity.
     *
     * @see JobEventMessage
     */
    public void processJobEvent(JobEventMessage jobEventMessage) {
        Optional<Job> existingJob = jobRepository.findByJobId(jobEventMessage.getJobId());

        if (existingJob.isPresent()) {
            Job job = existingJob.get();
            jobMapper.updateExistingJob(job, jobEventMessage);
            jobRepository.save(job);
        } else {
            try {
                Job newJob = jobMapper.toEntity(jobEventMessage);
                jobRepository.save(newJob);
            } catch (Exception e) {
                logger.error("❌ Failed to save job: {}", jobEventMessage, e);
            }
        }
        JobEvent eventEntity = jobMapper.toJobEvent(jobEventMessage);
        try{
            logger.info("Saving job event: {}", eventEntity);
            jobEventRepository.save(eventEntity);
        } catch (Exception e) {
            logger.error("❌ Failed to save job event: {}", eventEntity, e);
        }
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public List<Job> getJobsByUserId(Long userId) {
        return jobRepository.findByUserId(userId);
    }
    public List<Job> getActiveJobsByUserId(Long userId){
        return jobRepository.findByUserIdAndStatusNotLike(userId, JobStatusType.FINISHED);
    }
    public List<Job> getRecentJobsByUserId(Long userId, Integer timeframe) {
        int hours = (timeframe != null && timeframe > 0) ? timeframe : 24;
        Instant cutoff = Instant.now().minusSeconds(hours * 3600L);
        return jobRepository.findByUserIdAndCreatedAtAfter(userId, cutoff);
    }

    public Optional<Job> getJobById(Long id) {
        return jobRepository.findById(id);
    }

    public List<JobEvent> getJobEvents(Long id) {
        Job job = jobRepository.findById(id).orElseThrow(() -> new NotFoundException("Job not found"));
        return jobEventRepository.findByJobIdOrderByTimestampAsc(job.getJobId());
    }

}
