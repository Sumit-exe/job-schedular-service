package com.airtribe.jobschedular.dto.response;
import com.airtribe.jobschedular.util.enums.ExecutionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class JobExecutionHistoryResponse {

    private Instant executionTime;

    private ExecutionStatus status;

    private Integer retryCount;

    private Instant lastUpdateTime;

    private String errorMessage;
}
