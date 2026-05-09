package com.airtribe.jobschedular.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// keep identical in both services (or extract common module)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobEvent {
    private String jobId;
    private String payload;
    private int retryCount;
}