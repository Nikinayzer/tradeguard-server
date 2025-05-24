package korn03.tradeguardserver.service.job;

import korn03.tradeguardserver.endpoints.dto.user.job.JobFrontendDTO;
import korn03.tradeguardserver.endpoints.dto.user.job.UserJobsStateDTO;
import korn03.tradeguardserver.exception.NotFoundException;
import korn03.tradeguardserver.kafka.events.jobUpdates.JobEventMessage;
import korn03.tradeguardserver.kafka.events.jobUpdates.JobEventType;
import korn03.tradeguardserver.mapper.JobMapper;
import korn03.tradeguardserver.model.entity.job.Job;
import korn03.tradeguardserver.model.entity.job.JobEvent;
import korn03.tradeguardserver.model.entity.job.JobStatusType;
import korn03.tradeguardserver.model.repository.job.JobEventRepository;
import korn03.tradeguardserver.model.repository.job.JobRepository;
import korn03.tradeguardserver.service.sse.SseEmitterService;
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
    private final SseEmitterService sseService;

    private final Logger logger = LoggerFactory.getLogger(JobService.class);

    /**
     * Handles a job event from Kafka.
     * Updates the Job entity if it exists; otherwise, creates a new Job entity.
     * Always creates and saves a JobEvent entity.
     * Sends SSE update with new jobs state.
     *
     * @see JobEventMessage
     */
    public void processJobEvent(JobEventMessage jobEventMessage) {
        Optional<Job> existingJob = jobRepository.findByJobId(jobEventMessage.getJobId());

        if (existingJob.isPresent()) {
            Job job = existingJob.get();
            jobMapper.updateExistingJob(job, jobEventMessage);
            jobRepository.save(job);
            
            // Send SSE update with new jobs state
            UserJobsStateDTO jobsState = getUserJobsState(job.getUserId());
            if (jobsState != null) {
                sseService.sendUpdate(job.getUserId(), "jobs", jobsState);
            }
        } else {
            try {
                Long userId = null;
                if (jobEventMessage.getJobEventType() instanceof JobEventType.Created created) {
                    userId = created.meta().userId();
                }
                
                if (userId == null) {
                    logger.error("Cannot create new job without userId for jobId: {}", jobEventMessage.getJobId());
                    return;
                }
                
                Job newJob = jobMapper.toEntity(jobEventMessage);
                jobRepository.save(newJob);
                
                // Send SSE update with new jobs state
                UserJobsStateDTO jobsState = getUserJobsState(userId);
                if (jobsState != null) {
                    sseService.sendUpdate(userId, "jobs", jobsState);
                }
            } catch (Exception e) {
                logger.error("❌ Failed to save job: {}", jobEventMessage, e);
            }
        }
        
        JobEvent eventEntity = jobMapper.toJobEvent(jobEventMessage);
        try {
            logger.info("Saving job event: {}", eventEntity);
            jobEventRepository.save(eventEntity);
        } catch (Exception e) {
            logger.error("❌ Failed to save job event: {}", eventEntity, e);
        }
    }

    /**
     * Get the complete jobs state for a user including active jobs and summary statistics.
     * 
     * @param userId The user ID
     * @return A DTO containing complete jobs state information
     */
    public UserJobsStateDTO getUserJobsState(Long userId) {
        List<Job> allJobs = getJobsByUserId(userId);
        if (allJobs.isEmpty()) {
            return null;
        }
        
        List<Job> activeJobs = getActiveJobsByUserId(userId);
        Instant latestTimestamp = findLatestTimestamp(allJobs);
        
        return UserJobsStateDTO.builder()
                .summary(UserJobsStateDTO.JobsSummary.builder()
                    .totalJobsCount(allJobs.size())
                    .activeJobsCount(activeJobs.size())
                    .lastUpdate(latestTimestamp)
                    .build())
                .activeJobs(jobMapper.toFrontendDTOList(activeJobs))
                .build();
    }

    private Instant findLatestTimestamp(List<Job> jobs) {
        return jobs.stream()
                .map(Job::getUpdatedAt)
                .max(Instant::compareTo)
                .orElse(Instant.now());
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
