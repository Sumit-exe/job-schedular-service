package com.airtribe.jobschedular.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class CreateJobResponse {
    private UUID jobId;
    private String message;
}