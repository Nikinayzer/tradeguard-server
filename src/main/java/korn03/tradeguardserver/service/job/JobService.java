package korn03.tradeguardserver.service.job;

import korn03.tradeguardserver.kafka.events.JobEventMessage;
import korn03.tradeguardserver.mapper.JobMapper;
import korn03.tradeguardserver.model.entity.job.Job;
import korn03.tradeguardserver.model.entity.job.JobEvent;
import korn03.tradeguardserver.model.entity.job.JobStatusType;
import korn03.tradeguardserver.model.repository.job.JobEventRepository;
import korn03.tradeguardserver.model.repository.job.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final JobEventRepository jobEventRepository;
    private final JobMapper jobMapper;

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
            Job newJob = jobMapper.toEntity(jobEventMessage);
            jobRepository.save(newJob);
        }

        JobEvent eventEntity = jobMapper.toJobEvent(jobEventMessage);
        jobEventRepository.save(eventEntity);
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

    public Optional<Job> getJobById(Long jobId) {
        return jobRepository.findByJobId(jobId);
    }

    public List<JobEvent> getJobEvents(Long jobId) {
        return jobEventRepository.findByJobIdOrderByTimestampAsc(jobId);
    }

}
