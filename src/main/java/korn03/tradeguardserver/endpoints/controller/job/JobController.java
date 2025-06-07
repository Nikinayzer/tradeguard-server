package korn03.tradeguardserver.endpoints.controller.job;

import jakarta.validation.Valid;
import korn03.tradeguardserver.endpoints.dto.user.job.DcaJobSubmissionDTO;
import korn03.tradeguardserver.endpoints.dto.user.job.JobDTO;
import korn03.tradeguardserver.endpoints.dto.user.job.JobSubmissionDTO;
import korn03.tradeguardserver.endpoints.dto.user.job.LiqJobSubmissionDTO;
import korn03.tradeguardserver.model.entity.job.Job;
import korn03.tradeguardserver.model.entity.user.User;
import korn03.tradeguardserver.model.entity.user.connections.UserDiscordAccount;
import korn03.tradeguardserver.security.AuthUtil;
import korn03.tradeguardserver.service.core.PlatformService;
import korn03.tradeguardserver.service.job.JobCommandService;
import korn03.tradeguardserver.service.job.JobService;
import korn03.tradeguardserver.service.user.connection.UserDiscordAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

//todo DTO, JWT handling
@RestController
@RequiredArgsConstructor
@Slf4j
public class JobController {
    private final JobService jobService;
    private final JobCommandService jobCommandService;
    private final PlatformService platformService;
    private final UserDiscordAccountService userDiscordAccountService;

    @PostMapping("/jobs/dca")
    public CompletableFuture<ResponseEntity<Map<String, String>>> submitDcaJob(
            @RequestBody @Valid DcaJobSubmissionDTO request,
            @RequestHeader("X-Platform-Type") String platformType) {

        //todo testing, don't want to spam TR
//        return CompletableFuture.completedFuture(
//                ResponseEntity.accepted().body(Map.of("status", "ACCEPTED", "message", "DCA job submission accepted"))
//        );
        return submitJob(request, platformType, "DCA", jobCommandService::sendCreatedDcaJob);
    }

    @PostMapping("/jobs/liq")
    public CompletableFuture<ResponseEntity<Map<String, String>>> submitLiqJob(
            @RequestBody @Valid LiqJobSubmissionDTO request,
            @RequestHeader("X-Platform-Type") String platformType) {

        return submitJob(request, platformType, "LIQ", jobCommandService::sendCreatedLiqJob);
    }

    private <T extends JobSubmissionDTO> CompletableFuture<ResponseEntity<Map<String, String>>> submitJob(
            T request,
            String platformType,
            String name,
            BiFunction<T, Long, CompletableFuture<Void>> jobSender) {

        User user = AuthUtil.getCurrentUser();
        String source = platformService.resolveSource(platformType);
        request.setName(name);
        request.setSource(source);

        Long userId = user.getId();
        Optional<UserDiscordAccount> discord = userDiscordAccountService.getDiscordAccount(userId);
        if (discord.isPresent()) {
            userId = discord.get().getDiscordId();
        }
        log.info("REC {} job for userId: {}", name, userId);
        return jobSender.apply(request, userId)
                .thenApply(result -> ResponseEntity.accepted()
                        .body(Map.of("status", "ACCEPTED", "message", name + " job submission accepted")))
                .exceptionally(ex -> {
                    log.error("Job submission error", ex);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("status", "REJECTED", "message", ex.getMessage()));
                });
    }

    @PostMapping("/jobs/{id}/pause")
    public ResponseEntity<?> pauseJob(@PathVariable Long id, @RequestHeader("X-Platform-Type") String platformType) {
        User user = AuthUtil.getCurrentUser();
        String source = platformService.resolveSource(platformType);
        jobCommandService.sendPauseEvent(id, user.getId(), source);
        return ResponseEntity.accepted().body(Map.of("status", "PAUSED"));
    }

    @PostMapping("/jobs/{id}/resume")
    public ResponseEntity<?> resumeJob(@PathVariable Long id, @RequestHeader("X-Platform-Type") String platformType) {
        User user = AuthUtil.getCurrentUser();
        String source = platformService.resolveSource(platformType);
        jobCommandService.sendResumeEvent(id, user.getId(), source);
        return ResponseEntity.accepted().body(Map.of("status", "RESUMED"));
    }

    @PostMapping("/jobs/{id}/stop")
    public ResponseEntity<?> stopJob(@PathVariable Long id, @RequestHeader("X-Platform-Type") String platformType) {
        User user = AuthUtil.getCurrentUser();
        String source = platformService.resolveSource(platformType);
        jobCommandService.sendStopEvent(id, user.getId(), source);
        return ResponseEntity.accepted().body(Map.of("status", "STOPPED"));
    }


    @PostMapping("/jobs/{id}/cancel")
    public ResponseEntity<?> cancelJob(@PathVariable Long id, @RequestHeader("X-Platform-Type") String platformType) {
        User user = AuthUtil.getCurrentUser();
        String source = platformService.resolveSource(platformType);
        jobCommandService.sendCancelEvent(id, user.getId(), source);
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
        Long userId = AuthUtil.getCurrentUser().getId();
        List<Job> jobs = jobService.getJobsByUserId(userId);
        return ResponseEntity.ok(jobs);
    }

    /**
     * Get user's active jobs
     *
     * @return list of active jobs
     */
    @Deprecated
    @GetMapping("/users/jobs/active")
    public ResponseEntity<List<Job>> getUserActiveJobs() {
        Long userId = AuthUtil.getCurrentUser().getId();
        List<Job> jobs = jobService.getActiveJobsByUserId(userId);
        return ResponseEntity.ok(jobs);

    }

    @GetMapping("users/jobs/completed")
    public ResponseEntity<List<JobDTO>> getUserCompletedJobs() {
        Long userId = AuthUtil.getCurrentUser().getId();
        return ResponseEntity.ok( jobService.getCompletedJobsDTOByUserId(userId));
    }

    /**
     * Get recent jobs of user within timeframe
     * Example: GET /jobs/{userId}?timeframe=24
     */
    @GetMapping("/jobs/user/{userId}")
    public ResponseEntity<List<Job>> getRecentJobsByUserId(@PathVariable Long userId, @RequestParam(required = false) Integer timeframe) {
        return ResponseEntity.ok(jobService.getRecentJobsByUserId(userId, timeframe));
    }

    /**
     * Get job details by jobId.
     */
    @GetMapping("/jobs/{id}")
    public ResponseEntity<Job> getJob(@PathVariable Long id) {
        Optional<Job> job = jobService.getJobEntityById(id);
        return job.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}