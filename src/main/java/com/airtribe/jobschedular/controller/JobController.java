package com.airtribe.jobschedular.controller;

import com.airtribe.jobschedular.dto.request.CreateJobRequest;
import com.airtribe.jobschedular.dto.response.CreateJobResponse;
import com.airtribe.jobschedular.dto.response.JobExecutionHistoryResponse;
import com.airtribe.jobschedular.dto.response.JobResponse;
import com.airtribe.jobschedular.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    private UUID getUserId(ServerWebExchange exchange) {

        String userId = exchange.getAttribute("userId");

        if (userId == null) {
            throw new RuntimeException("Unauthorized");
        }

        return UUID.fromString(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CreateJobResponse> createJob(ServerWebExchange exchange,
                                             @RequestBody CreateJobRequest request) {
        return jobService.createJob(request, getUserId(exchange));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Mono<List<JobResponse>> getJobs(ServerWebExchange exchange) {
        return jobService.getUserJobs(getUserId(exchange));
    }

    @GetMapping("/{jobId}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<JobResponse> getJob(@PathVariable UUID jobId,
                                    ServerWebExchange exchange) {
        return jobService.getJob(getUserId(exchange), jobId);
    }

    @PutMapping("/{jobId}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<JobResponse> updateJob(@PathVariable UUID jobId,
                                       @RequestBody CreateJobRequest request,
                                       ServerWebExchange exchange) {
        return jobService.updateJob(getUserId(exchange), jobId, request);
    }

    /**
     * API to Pause/Resume job
     * @param jobId
     * @param exchange
     * @return
     */
    @PatchMapping("/{jobId}/toggle-job-status")
    @ResponseStatus(HttpStatus.OK)
    public Mono<JobResponse> toggleJobStatus(@PathVariable UUID jobId, @RequestParam boolean active,
                                      ServerWebExchange exchange) {
        return jobService.updateJobStatus(getUserId(exchange), jobId, active);
    }


    @DeleteMapping("/{jobId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteJob(@PathVariable UUID jobId,
                                ServerWebExchange exchange) {
        return jobService.deleteJob(getUserId(exchange), jobId);
    }

    @GetMapping("/{jobId}/executions")
    @ResponseStatus(HttpStatus.OK)
    public Mono<List<JobExecutionHistoryResponse>>
    getExecutionHistory(
            @PathVariable UUID jobId,
            ServerWebExchange exchange
    ) {

        return jobService.getExecutionHistory(
                getUserId(exchange),
                jobId
        );
    }
}