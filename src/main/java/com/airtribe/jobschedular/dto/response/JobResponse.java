package com.airtribe.jobschedular.dto.response;

import com.airtribe.jobschedular.util.enums.ExecutionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class JobResponse {

    private UUID jobId;
    private Boolean isRecurring;
    private String interval;
    private Integer maxRetryCount;
    private Instant createdTime;
    private String payload;
    private ExecutionStatus executionStatus;
    private boolean isActive;
}