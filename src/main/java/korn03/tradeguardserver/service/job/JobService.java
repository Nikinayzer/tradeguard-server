package korn03.tradeguardserver.service.job;

import korn03.tradeguardserver.endpoints.dto.user.job.JobDTO;
import korn03.tradeguardserver.endpoints.dto.user.job.UserJobsStateDTO;
import korn03.tradeguardserver.exception.NotFoundException;
import korn03.tradeguardserver.kafka.events.jobUpdates.JobEventMessage;
import korn03.tradeguardserver.kafka.events.jobUpdates.JobEventType;
import korn03.tradeguardserver.mapper.JobMapper;
import korn03.tradeguardserver.model.entity.job.Job;
import korn03.tradeguardserver.model.entity.job.JobEvent;
import korn03.tradeguardserver.model.entity.job.JobStatusType;
import korn03.tradeguardserver.model.entity.service.notifications.NotificationCategory;
import korn03.tradeguardserver.model.entity.service.notifications.NotificationType;
import korn03.tradeguardserver.model.repository.job.JobEventRepository;
import korn03.tradeguardserver.model.repository.job.JobRepository;
import korn03.tradeguardserver.service.core.CacheService;
import korn03.tradeguardserver.service.core.pushNotifications.PushNotificationService;
import korn03.tradeguardserver.service.sse.SseEmitterService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService {

    private static final String JOB_KEY_PATTERN = "job:%d";
    private static final Duration JOB_TTL = Duration.ofHours(48);

    private final JobRepository jobRepository;
    private final JobEventRepository jobEventRepository;
    private final JobMapper jobMapper;
    private final SseEmitterService sseService;
    private final CacheService cacheService;

    private final Logger logger = LoggerFactory.getLogger(JobService.class);
    private final PushNotificationService pushNotificationService;

    /**
     * Handles a real-time job event from Kafka.
     * Updates the Job entity if it exists; otherwise, creates a new Job entity.
     * Always sends SSE update with new jobs state.
     *
     * @see JobEventMessage
     */
    public void processJobEvent(JobEventMessage jobEventMessage) {
//        Optional<Job> existingJob = jobRepository.findByJobId(jobEventMessage.getJobId())
//                .or(() -> Optional.ofNullable(
//                        cacheService.getFromCache(String.format(JOB_KEY_PATTERN, jobEventMessage.getJobId()), Job.class)
//                ));
        Optional<Job> existingJob = Optional.ofNullable(
                cacheService.getFromCache(String.format(JOB_KEY_PATTERN, jobEventMessage.getJobId()), Job.class)
        );

        if (existingJob.isPresent()) {
            Job job = existingJob.get();
            jobMapper.updateExistingJob(job, jobEventMessage);

            if (job.getStatus().isActive()) {
                String jobKey = String.format(JOB_KEY_PATTERN, job.getJobId());
                cacheService.storeInCache(jobKey, job, JOB_TTL);
            } else {
                jobRepository.save(job);
                String jobKey = String.format(JOB_KEY_PATTERN, job.getJobId());
                cacheService.deleteFromCache(jobKey);
            }

            UserJobsStateDTO jobsState = getUserJobsState(job.getUserId());
            if (jobsState != null) {
                sendJobPushNotification(job, jobEventMessage.getJobEventType());
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
                String jobKey = String.format(JOB_KEY_PATTERN, newJob.getJobId());
                cacheService.storeInCache(jobKey, newJob, JOB_TTL);

                UserJobsStateDTO jobsState = getUserJobsState(userId);
                if (jobsState != null) {
                    sseService.sendUpdate(userId, "jobs", jobsState);
                }
            } catch (Exception e) {
                logger.error("❌ Failed to save job: {}", jobEventMessage, e);
            }
        }
    }

    /**
     * Handles a historical job event from Kafka.
     * Updates the Job entity if it exists; otherwise, creates a new Job entity.
     *
     * @param jobEventMessage The job event message to process
     */
    public void processHistoricalJobEvent(JobEventMessage jobEventMessage) {
//        Optional<Job> existingJob = jobRepository.findByJobId(jobEventMessage.getJobId())
//                .or(() -> Optional.ofNullable(
//                        cacheService.getFromCache(String.format(JOB_KEY_PATTERN, jobEventMessage.getJobId()), Job.class)
//                ));
        Optional<Job> existingJob = Optional.ofNullable(
                cacheService.getFromCache(String.format(JOB_KEY_PATTERN, jobEventMessage.getJobId()), Job.class)
        );

        if (existingJob.isPresent()) {
            Job job = existingJob.get();
            jobMapper.updateExistingJob(job, jobEventMessage);

            if (job.getStatus().isActive()) {
                String jobKey = String.format(JOB_KEY_PATTERN, job.getJobId());
                cacheService.storeInCache(jobKey, job, JOB_TTL);
            } else {
                jobRepository.save(job);
                String jobKey = String.format(JOB_KEY_PATTERN, job.getJobId());
                cacheService.deleteFromCache(jobKey);
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
                String jobKey = String.format(JOB_KEY_PATTERN, newJob.getJobId());
                cacheService.storeInCache(jobKey, newJob, JOB_TTL);
            } catch (Exception e) {
                logger.error("❌ Failed to save historical job: {}", jobEventMessage, e);
            }
        }
    }

    /**
     * Get the complete jobs state for a user including active jobs and summary statistics.
     *
     * @param userId The user ID
     * @return A DTO containing complete jobs state information
     */
    public UserJobsStateDTO getUserJobsState(Long userId) {
        List<Job> activeJobs = getActiveJobsByUserId(userId);
        List<Job> completedJobs = getCompletedJobsEntitiesByUserId(userId);
        List<Job> allJobs = new ArrayList<>(activeJobs);
        allJobs.addAll(completedJobs);
        if (allJobs.isEmpty()) {
            return null;
        }
        Instant latestTimestamp = findLatestTimestamp(allJobs);
        return UserJobsStateDTO.builder()
                .summary(UserJobsStateDTO.JobsSummary.builder()
                        .totalJobsCount(allJobs.size())
                        .activeJobsCount(activeJobs.size())
                        .lastUpdate(latestTimestamp)
                        .build())
                .activeJobs(jobMapper.toDTOList(activeJobs))
                .build();
    }

    private Instant findLatestTimestamp(List<Job> jobs) {
        return jobs.stream()
                .map(Job::getUpdatedAt)
                .max(Instant::compareTo)
                .orElse(Instant.now());
    }

    public List<Job> getAllJobs() {
        Set<Job> allJobs = new HashSet<>(jobRepository.findAll());
        Set<String> jobKeys = cacheService.getKeys("job:*");
        for (String key : jobKeys) {
            Job cachedJob = cacheService.getFromCache(key, Job.class);
            if (cachedJob != null) {
                allJobs.add(cachedJob);
            }
        }
        return allJobs.stream()
                .sorted(Comparator.comparing(Job::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<Job> getJobsByUserId(Long userId) {
        return jobRepository.findByUserId(userId);
    }

    public List<Job> getCompletedJobsEntitiesByUserId(Long userId) {
        List<JobStatusType> completedStatuses = List.of(JobStatusType.FINISHED, JobStatusType.CANCELED, JobStatusType.STOPPED);
        return jobRepository.findByUserIdAndStatusIn(userId, completedStatuses);
    }

    public List<JobDTO> getCompletedJobsDTOByUserId(Long userId) {
        List<Job> jobs = getCompletedJobsEntitiesByUserId(userId);
        return jobMapper.toDTOList(jobs);
    }

    public List<Job> getActiveJobsByUserId(Long userId) {
        Set<String> jobKeys = cacheService.getKeys("job:*");
        return jobKeys.stream()
                .map(key -> cacheService.getFromCache(key, Job.class))
                .filter(Objects::nonNull)
                .filter(job -> job.getUserId().equals(userId))
                .filter(job -> job.getStatus().isActive())
                .collect(Collectors.toList());
    }

    public List<Job> getRecentJobsByUserId(Long userId, Integer timeframe) {
        int hours = (timeframe != null && timeframe > 0) ? timeframe : 24;
        Instant cutoff = Instant.now().minusSeconds(hours * 3600L);
        List<Job> activeJobs = getActiveJobsByUserId(userId);
        List<Job> completedJobs = jobRepository.findByUserIdAndCreatedAtAfter(userId, cutoff);
        List<Job> allJobs = new ArrayList<>(activeJobs);
        allJobs.addAll(completedJobs);
        return allJobs;
    }

    public Optional<Job> getJobEntityById(Long id) {
        String jobKey = String.format(JOB_KEY_PATTERN, id);
        Optional<Job> cachedJob = Optional.ofNullable(cacheService.getFromCache(jobKey, Job.class));
        if (cachedJob.isPresent()) {
            return cachedJob;
        }
        return jobRepository.findById(id);
    }

    public List<JobEvent> getJobEvents(Long id) {
        Job job = jobRepository.findById(id).orElseThrow(() -> new NotFoundException("Job not found"));
        return jobEventRepository.findByJobIdOrderByTimestampAsc(job.getJobId());
    }

    private void sendJobPushNotification(Job job, JobEventType eventType) {
        String title;
        String  message;
        NotificationType notificationType = NotificationType.INFO;
        switch (eventType) {
            case JobEventType.StepDone stepDone -> {
                title = String.format("Job %s update", job.getJobId());
                message = String.format(
                        "Job %s has completed step %d of %d",
                        job.getJobId(),
                        job.getStepsDone(),
                        job.getStepsTotal()
                );
            }
            case JobEventType.OrdersPlaced ordersPlaced -> {
                title = String.format("Job %s orders placed", job.getJobId());
                String coins = job.getCoins().stream().map(String::toUpperCase).collect(Collectors.joining(", "));
                message = String.format(
                        "Job %s has successfully placed %d orders for %s",
                        job.getJobId(),
                        ordersPlaced.orders().size(),
                        coins
                );
            }
            case JobEventType.Finished finished-> {
                title = String.format("Job %s finished", job.getJobId());
                message = String.format(
                        "Job %s has finished successfully",
                        job.getJobId()
                );
            }
            case JobEventType.ErrorEvent errorEvent -> {
                title = String.format("Error in Job %s", job.getJobId());
                message = String.format(
                        "An error occurred in Job %s: %s",
                        job.getJobId(),
                        errorEvent.message()
                );
            }
            default -> {
                return;
            }
        }

        pushNotificationService.sendPushNotification(
                job.getUserId(),
                NotificationCategory.JOB,
                notificationType,
                title,
                message
        );
    }

}
