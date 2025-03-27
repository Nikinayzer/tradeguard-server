package korn03.tradeguardserver.endpoints.controller.job;

import korn03.tradeguardserver.endpoints.dto.user.job.DcaJobSubmissionDTO;
import korn03.tradeguardserver.endpoints.dto.user.job.LiqJobSubmissionDTO;
import korn03.tradeguardserver.kafka.producer.JobSubmissionProducer;
import korn03.tradeguardserver.mapper.JobSubmissionMapper;
import korn03.tradeguardserver.model.entity.job.Job;
import korn03.tradeguardserver.model.entity.job.JobEvent;
import korn03.tradeguardserver.model.entity.user.User;
import korn03.tradeguardserver.service.job.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

//todo DTO, JWT handling
@RestController
@RequiredArgsConstructor
@Slf4j
public class JobController {
    private final JobService jobService;
    private final JobSubmissionProducer jobSubmissionProducer;
    private final JobSubmissionMapper jobSubmissionMapper;

    @PostMapping("/jobs/dca")
    public ResponseEntity<Map<String, String>> submitDcaJob(@RequestBody DcaJobSubmissionDTO request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        log.info("Received DCA job submission request from user: {}", user.getUsername());

        CompletableFuture<Void> future = jobSubmissionProducer.sendJobSubmission(jobSubmissionMapper.toDcaMessage(request, user.getId())).thenAccept(result -> log.info("DCA job submitted successfully to partition: {}, offset: {}", result.getRecordMetadata().partition(), result.getRecordMetadata().offset()));

        Map<String, String> response = new HashMap<>();
        response.put("status", "ACCEPTED");
        response.put("message", "DCA job submission accepted");
        response.put("type", "DCA");

        return ResponseEntity.accepted().body(response);
    }

    @PostMapping("/jobs/liq")
    public ResponseEntity<Map<String, String>> submitLiquidationJob(@RequestBody LiqJobSubmissionDTO request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        log.info("Received liquidation job submission request from user: {}", user.getUsername());

        CompletableFuture<Void> future = jobSubmissionProducer.sendJobSubmission(jobSubmissionMapper.toLiqMessage(
                request,
                user.getId())
                )
                .thenAccept
                        (result -> log.info("Liquidation job submitted successfully to partition: {}, offset: {}", result.getRecordMetadata().partition(), result.getRecordMetadata().offset()));

        Map<String, String> response = new HashMap<>();
        response.put("status", "ACCEPTED");
        response.put("message", "Liquidation job submission accepted");
        response.put("type", "LIQUIDATION");

        return ResponseEntity.accepted().body(response);
    }

    /**
     * Get all jobs.
     *
     * @return List of all jobs.
     */
    @GetMapping("/jobs")
    public ResponseEntity<List<Job>> getJobs() {
        List<Job> jobs = jobService.getAllJobs();
        return ResponseEntity.ok(jobs);
    }

    /**
     * Get user's jobs
     *
     * @return list of user jobs
     */
    @GetMapping("/users/jobs")
    public ResponseEntity<List<Job>> getUserJobs() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        List<Job> jobs = jobService.getJobsByUserId(user.getId());
        return ResponseEntity.ok(jobs);
    }

    /**
     * Get user's active jobs
     *
     * @return list of active jobs
     */
    @GetMapping("/users/jobs/active")
    public ResponseEntity<List<Job>> getUserActiveJobs() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        List<Job> jobs = jobService.getActiveJobsByUserId(user.getId());
        return ResponseEntity.ok(jobs);

    }
    /**
     * Get recent jobs of user within timeframe
     * Example: GET /jobs/{userId}?timeframe=24
     * USED BY HEALTH MODULE TO  LOAD CONTEXT OF USER JOBS
     * /todo integrate DTOs instead of entity (for whole controller)
     */
    @GetMapping("/jobs/user/{userId}")
    public ResponseEntity<?> getRecentJobsByUserId(@PathVariable Long userId, @RequestParam(required = false) Integer timeframe) {
        return ResponseEntity.ok(jobService.getRecentJobsByUserId(userId, timeframe));
    }


    /**
     * Get job details by jobId.
     */
    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<Job> getJob(@PathVariable Long jobId) {
        Optional<Job> job = jobService.getJobById(jobId);
        return job.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get all events for a given jobId.
     */
    @GetMapping("/jobs/{jobId}/events")
    public ResponseEntity<List<JobEvent>> getJobEvents(@PathVariable Long jobId) {
        List<JobEvent> events = jobService.getJobEvents(jobId);
        return ResponseEntity.ok(events);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        log.error("Validation error in job submission: {}", errors);
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());
        error.put("type", e.getClass().getSimpleName());

        log.error("Error processing job submission", e);
        return ResponseEntity.badRequest().body(error);
    }
}