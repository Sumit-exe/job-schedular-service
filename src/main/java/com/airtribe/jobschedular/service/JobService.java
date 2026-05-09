package com.airtribe.jobschedular.service;

import com.airtribe.jobschedular.dto.request.CreateJobRequest;
import com.airtribe.jobschedular.dto.response.CreateJobResponse;
import com.airtribe.jobschedular.dto.response.JobExecutionHistoryResponse;
import com.airtribe.jobschedular.dto.response.JobResponse;
import com.airtribe.jobschedular.entity.Job;
import com.airtribe.jobschedular.entity.TaskExecutionHistory;
import com.airtribe.jobschedular.entity.TaskSchedule;
import com.airtribe.jobschedular.repository.JobRepository;
import com.airtribe.jobschedular.repository.TaskExecutionHistoryRepository;
import com.airtribe.jobschedular.repository.TaskScheduleRepository;
import com.airtribe.jobschedular.util.BucketUtil;
import com.airtribe.jobschedular.util.enums.ExecutionStatus;
import com.airtribe.jobschedular.util.enums.JobType;
import com.airtribe.jobschedular.util.enums.ScheduleType;
import com.airtribe.jobschedular.util.enums.SupportedTimezone;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {

    private final JobRepository jobRepository;

    private final TaskScheduleRepository taskScheduleRepository;

    private final TaskExecutionHistoryRepository taskExecutionHistoryRepository;

    public Mono<CreateJobResponse> createJob(CreateJobRequest request, UUID userId) {

        return Mono.fromCallable(() -> {

            UUID jobId = UUID.randomUUID();

            Job job = buildJobEntity(request, userId, jobId);

            jobRepository.save(job);

//            createOrUpdateTaskSchedule(job);
            scheduleJob(job, calculateNextExecution(job));

            return new CreateJobResponse(jobId, "Job created successfully");

        }).subscribeOn(Schedulers.boundedElastic());
    }

    private void scheduleJob(Job job, Instant nextExecution) {

        String bucket = BucketUtil.getBucket(
                LocalDateTime.ofInstant(nextExecution, ZoneOffset.UTC)
        );

        TaskSchedule schedule = new TaskSchedule();

        TaskSchedule.Key key = new TaskSchedule.Key();

        key.setExecutionBucket(bucket);
        key.setJobId(job.getKey().getJobId());

        schedule.setKey(key);
        schedule.setNextExecutionTime(nextExecution);
        schedule.setStatus(ExecutionStatus.SCHEDULED);
        schedule.setPayload(job.getPayload());
        schedule.setIsActive(job.getIsActive());

        taskScheduleRepository.save(schedule);
    }

    private Job buildJobEntity(CreateJobRequest request, UUID userId, UUID jobId) {

        Job job = new Job();

        Job.Key key = new Job.Key();

        key.setUserId(userId);
        key.setJobId(jobId);
        job.setKey(key);

        updateJobFields(job, request);

        job.setCreatedTime(Instant.now());

        job.setExecutionStatus(ExecutionStatus.SCHEDULED);

        job.setIsActive(true);

        return job;
    }

    private void updateJobFields(Job job, CreateJobRequest request) {

        job.setIsRecurring(Boolean.TRUE.equals(request.getRecurring()));
        job.setScheduleType(request.getScheduleType());
        job.setExecuteAt(request.getExecuteAt());
        job.setFrequency(request.getFrequency());
        job.setTimeUnit(request.getTimeUnit());
        job.setCronExpression(request.getCronExpression());
        job.setTimezone(request.getTimezone() == null ? SupportedTimezone.IST : request.getTimezone());
        job.setMaxRetryCount(request.getMaxRetryCount());
        job.setPayload(request.getPayload());
    }

    private void createOrUpdateTaskSchedule(Job job) {

        Instant nextExecution = calculateNextExecution(job);

        String bucket = BucketUtil.getBucket(LocalDateTime.ofInstant(nextExecution, ZoneOffset.UTC));

        TaskSchedule schedule = new TaskSchedule();

        TaskSchedule.Key key = new TaskSchedule.Key();

        key.setExecutionBucket(bucket);
        key.setJobId(job.getKey().getJobId());

        schedule.setKey(key);
        schedule.setNextExecutionTime(nextExecution);
        schedule.setStatus(ExecutionStatus.SCHEDULED);
        schedule.setPayload(job.getPayload());
        schedule.setIsActive(job.getIsActive());

        taskScheduleRepository.save(schedule);
    }

    private void deleteExistingSchedule(Job job) {

        Instant oldExecution = calculateNextExecution(job);

        String bucket = BucketUtil.getBucket(LocalDateTime.ofInstant(oldExecution, ZoneOffset.UTC));

        TaskSchedule.Key key = new TaskSchedule.Key();

        key.setExecutionBucket(bucket);
        key.setJobId(job.getKey().getJobId());

        taskScheduleRepository.deleteById(key);
    }

    private Instant calculateNextExecution(Job job) {
        ZoneId zoneId = ZoneId.of(job.getTimezone().getZoneId());
        ZonedDateTime now = ZonedDateTime.now(zoneId);

        return switch (job.getScheduleType()) {
            case ONE_TIME_IMMEDIATE -> Instant.now();

            case ONE_TIME_SCHEDULED -> job.getExecuteAt();

            case RECURRING_FIXED_INTERVAL -> {
                ZonedDateTime next = switch (job.getTimeUnit()) {
                    case MINUTES -> now.plusMinutes(job.getFrequency());
                    case HOURS   -> now.plusHours(job.getFrequency());
                    case DAYS    -> now.plusDays(job.getFrequency());
                    case WEEKS   -> now.plusWeeks(job.getFrequency());
                    case MONTHS  -> now.plusMonths(job.getFrequency());
                    case YEARS   -> now.plusYears(job.getFrequency());
                };
                yield next.toInstant();          // ← yield, not return, inside a block arm
            }

            case RECURRING_CRON -> {
                CronExpression cron = CronExpression.parse(job.getCronExpression());
                LocalDateTime next = cron.next(LocalDateTime.now(zoneId));
                if (next == null) throw new RuntimeException("Invalid cron");
                yield next.atZone(zoneId).toInstant();
            }

            default -> throw new RuntimeException("Unsupported schedule type");
        };
    }

    public Mono<JobResponse> updateJob(
            UUID userId,
            UUID jobId,
            CreateJobRequest request
    ) {

        return Mono.fromCallable(() ->
                        jobRepository.findByKeyUserIdAndKeyJobId(userId, jobId))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalJob -> {

                    if (optionalJob.isEmpty()) {
                        return Mono.error(
                                new RuntimeException("Job not found")
                        );
                    }

                    Job job = optionalJob.get();

                    Instant oldExecution =
                            calculateNextExecution(job);

                    deleteSchedule(job, oldExecution);

                    updateJobFields(job, request);

                    jobRepository.save(job);

                    Instant newExecution =
                            calculateNextExecution(job);

                    scheduleJob(job, newExecution);

                    return Mono.just(mapToResponse(job));
                });
    }
    private void deleteSchedule(Job job, Instant executionTime) {

        String bucket = BucketUtil.getBucket(
                LocalDateTime.ofInstant(executionTime, ZoneOffset.UTC)
        );

        TaskSchedule.Key key = new TaskSchedule.Key();

        key.setExecutionBucket(bucket);
        key.setJobId(job.getKey().getJobId());

        taskScheduleRepository.deleteById(key);
    }

    public Mono<List<JobResponse>> getUserJobs(UUID userId) {

        return Mono.fromCallable(() -> jobRepository.findByKeyUserId(userId))
                .subscribeOn(Schedulers.boundedElastic())
                .map(jobs -> jobs.stream()
                        .map(this::mapToResponse)
                        .toList()
                );
    }

    public Mono<JobResponse> getJob(UUID userId, UUID jobId) {

        return Mono.fromCallable(() -> jobRepository.findByKeyUserIdAndKeyJobId(userId, jobId))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalJob -> {

                    if (optionalJob.isEmpty()) return Mono.error(new RuntimeException("Job not found"));

                    Job job = optionalJob.get();

                    return Mono.just(mapToResponse(job));
                });
    }

    public Mono<JobResponse> updateJobStatus(
            UUID userId,
            UUID jobId,
            boolean active
    ) {

        return Mono.fromCallable(() ->
                        jobRepository.findByKeyUserIdAndKeyJobId(userId, jobId))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalJob -> {

                    if (optionalJob.isEmpty()) {
                        return Mono.error(
                                new RuntimeException("Job not found")
                        );
                    }

                    Job job = optionalJob.get();

                    job.setIsActive(active);

                    jobRepository.save(job);

                    Instant execution =
                            calculateNextExecution(job);

                    String bucket = BucketUtil.getBucket(
                            LocalDateTime.ofInstant(
                                    execution,
                                    ZoneOffset.UTC
                            )
                    );

                    TaskSchedule schedule =
                            taskScheduleRepository
                                    .findById(
                                            buildTaskKey(
                                                    bucket,
                                                    jobId
                                            )
                                    )
                                    .orElse(null);

                    if (schedule != null) {

                        schedule.setIsActive(active);

                        taskScheduleRepository.save(schedule);
                    }

                    return Mono.just(mapToResponse(job));
                });
    }

    private TaskSchedule.Key buildTaskKey(
            String bucket,
            UUID jobId
    ) {

        TaskSchedule.Key key =
                new TaskSchedule.Key();

        key.setExecutionBucket(bucket);
        key.setJobId(jobId);

        return key;
    }

//    private TaskSchedule.Key buildTaskScheduleKey(String bucket, UUID jobId) {
//
//        TaskSchedule.Key key = new TaskSchedule.Key();
//
//        key.setExecutionBucket(bucket);
//
//        key.setJobId(jobId);
//
//        return key;
//    }

    public Mono<Void> deleteJob(UUID userId, UUID jobId) {

        return Mono.fromCallable(() -> jobRepository.findByKeyUserIdAndKeyJobId(userId, jobId))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalJob -> {

                    if (optionalJob.isEmpty()) return Mono.error(new RuntimeException("Job not found"));

                    Job job = optionalJob.get();

                    job.setExecutionStatus(ExecutionStatus.DELETED);
                    job.setIsActive(false);

                    return Mono.fromCallable(() -> {
                        jobRepository.save(job);
                        return true;
                    }).subscribeOn(Schedulers.boundedElastic()).then();
                });
    }

    private JobResponse mapToResponse(Job job) {

        return JobResponse.builder()
                .jobId(job.getKey().getJobId())
                .isRecurring(job.getIsRecurring())
                .interval(job.getInterval())
                .maxRetryCount(job.getMaxRetryCount())
                .payload(job.getPayload())
                .createdTime(job.getCreatedTime())
                .executionStatus(job.getExecutionStatus())
                .isActive(job.getIsActive())
                .build();
    }


    public Mono<List<JobExecutionHistoryResponse>>
    getExecutionHistory(
            UUID userId,
            UUID jobId
    ) {

        return Mono.fromCallable(() -> {

            Optional<Job> optionalJob =
                    jobRepository
                            .findByKeyUserIdAndKeyJobId(
                                    userId,
                                    jobId
                            );

            if (optionalJob.isEmpty()) {

                throw new RuntimeException(
                        "Job not found"
                );
            }

            return taskExecutionHistoryRepository
                    .findByKeyJobId(jobId)
                    .stream()
                    .map(this::mapExecutionHistory)
                    .toList();

        }).subscribeOn(Schedulers.boundedElastic());
    }

    private JobExecutionHistoryResponse
    mapExecutionHistory(
            TaskExecutionHistory history
    ) {

        return JobExecutionHistoryResponse
                .builder()
                .executionTime(
                        history.getKey()
                                .getExecutionTime()
                )
                .status(history.getStatus())
                .retryCount(
                        history.getRetryCount()
                )
                .lastUpdateTime(
                        history.getLastUpdateTime()
                )
                .errorMessage(
                        history.getErrorMessage()
                )
                .build();
    }

}
