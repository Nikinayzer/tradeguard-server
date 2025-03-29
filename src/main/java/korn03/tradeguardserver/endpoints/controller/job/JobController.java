package korn03.tradeguardserver.endpoints.controller.job;

import jakarta.validation.Valid;
import korn03.tradeguardserver.endpoints.dto.user.job.DcaJobSubmissionDTO;
import korn03.tradeguardserver.endpoints.dto.user.job.LiqJobSubmissionDTO;
import korn03.tradeguardserver.kafka.producer.JobSubmissionProducer;
import korn03.tradeguardserver.model.entity.job.Job;
import korn03.tradeguardserver.model.entity.job.JobEvent;
import korn03.tradeguardserver.model.entity.user.User;
import korn03.tradeguardserver.security.AuthUtil;
import korn03.tradeguardserver.service.core.PlatformService;
import korn03.tradeguardserver.service.job.JobCommandService;
import korn03.tradeguardserver.service.job.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
import java.util.concurrent.TimeUnit;

//todo DTO, JWT handling
@RestController
@RequiredArgsConstructor
@Slf4j
public class JobController {
    private final JobService jobService;
    private final JobSubmissionProducer jobSubmissionProducer;
    private final JobCommandService jobCommandService;
    private final PlatformService platformService;

    @PostMapping("/jobs/dca")
    public CompletableFuture<ResponseEntity<Map<String, String>>> submitDcaJob(@RequestBody @Valid DcaJobSubmissionDTO request, @RequestHeader("X-Platform-Type") String platformType) {
        User user = AuthUtil.getCurrentUser();
        String source = platformService.resolveSource(platformType);
        request.setSource(source);
        return jobCommandService.sendCreatedDcaJob(request, user.getId()).thenApply(result -> ResponseEntity.accepted().body(Map.of("status", "ACCEPTED", "message", "DCA job submission accepted"))).exceptionally(ex -> {
            log.error("Job submission error", ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status", "REJECTED", "message", ex.getMessage()));
        });
    }

    @PostMapping("/jobs/liq")
    public CompletableFuture<ResponseEntity<Map<String, String>>> submitLiqJob(@RequestBody @Valid LiqJobSubmissionDTO request, @RequestHeader("X-Platform-Type") String platformType) {
        User user = AuthUtil.getCurrentUser();
        String source = platformService.resolveSource(platformType);
        request.setSource(source);
        return jobCommandService.sendCreatedLiqJob(request, user.getId()).thenApply(result -> ResponseEntity.accepted().body(Map.of("status", "ACCEPTED", "message", "DCA job submission accepted"))).exceptionally(ex -> {
            log.error("Job submission error", ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status", "REJECTED", "message", ex.getMessage()));
        });
    }

    @PostMapping("/jobs/{jobId}/pause")
    public ResponseEntity<?> pauseJob(@PathVariable Long jobId, @RequestHeader("X-Platform-Type") String platformType) {
        User user = AuthUtil.getCurrentUser();
        String source = platformService.resolveSource(platformType);
        jobCommandService.sendPauseEvent(jobId, user.getId(), source);
        return ResponseEntity.accepted().body(Map.of("status", "PAUSED"));
    }

    @PostMapping("/jobs/{jobId}/resume")
    public ResponseEntity<?> resumeJob(@PathVariable Long jobId, @RequestHeader("X-Platform-Type") String platformType) {
        User user = AuthUtil.getCurrentUser();
        String source = platformService.resolveSource(platformType);
        jobCommandService.sendResumeEvent(jobId, user.getId(), source);
        return ResponseEntity.accepted().body(Map.of("status", "RESUMED"));
    }

    @PostMapping("/jobs/{jobId}/stop")
    public ResponseEntity<?> stopJob(@PathVariable Long jobId, @RequestHeader("X-Platform-Type") String platformType) {
        User user = AuthUtil.getCurrentUser();
        String source = platformService.resolveSource(platformType);
        jobCommandService.sendStopEvent(jobId, user.getId(), source);
        return ResponseEntity.accepted().body(Map.of("status", "STOPPED"));
    }


    @PostMapping("/{jobId}/cancel")
    public ResponseEntity<?> cancelJob(@PathVariable Long jobId, @RequestParam String source) {
        User user = AuthUtil.getCurrentUser();
        jobCommandService.sendCancelEvent(jobId, user.getId(), source);
        return ResponseEntity.accepted().body(Map.of("status", "CANCELED"));
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
    public ResponseEntity<List<Job>> getRecentJobsByUserId(@PathVariable Long userId, @RequestParam(required = false) Integer timeframe) {
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