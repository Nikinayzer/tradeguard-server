package korn03.tradeguardserver.service.job;

import korn03.tradeguardserver.kafka.events.JobEventMessage;
import korn03.tradeguardserver.model.entity.job.Job;
import korn03.tradeguardserver.model.entity.job.JobEvent;
import korn03.tradeguardserver.model.entity.job.JobStatus;
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

    /**
     * Handles a job event from Kafka
     * Creates job event entity and updates job entity if it exists, otherwise creates a new job entity.
     * @see JobEventMessage
     */
    public void processJobEvent(JobEventMessage jobEventMessage) {
        JobEvent eventEntity = JobEvent.fromJobEvent(jobEventMessage);
        jobEventRepository.save(eventEntity);

        Optional<Job> existingJob = jobRepository.findByJobId(jobEventMessage.getJobId());

        if (existingJob.isPresent()) {
            Job job = existingJob.get();
            job.setStatus(JobStatus.fromEventType(jobEventMessage.getJobEventType().getType()));
            job.setStepsDone(jobEventMessage.getStepsDone());
            job.setUpdatedAt(jobEventMessage.getTimestamp());
            jobRepository.save(job);
        } else {
            Job newJob = Job.fromJobEvent(jobEventMessage);
            jobRepository.save(newJob);
        }
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public List<Job> getJobsByUserId(Long userId) {
        return jobRepository.findByUserId(userId);
    }

    public Optional<Job> getJobById(Long jobId) {
        return jobRepository.findByJobId(jobId);
    }

    public List<JobEvent> getJobEvents(Long jobId) {
        return jobEventRepository.findByJobIdOrderByTimestampAsc(jobId);
    }
}
