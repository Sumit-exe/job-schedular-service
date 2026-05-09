package com.airtribe.jobschedular.dto.request;

import com.airtribe.jobschedular.util.enums.ScheduleType;
import com.airtribe.jobschedular.util.enums.SupportedTimezone;
import com.airtribe.jobschedular.util.enums.TimeUnit;
import lombok.Data;

import java.time.Instant;
@Data
public class CreateJobRequest {

    private Boolean recurring;

    private ScheduleType scheduleType;

    private Instant executeAt;

    private Integer frequency;

    private TimeUnit timeUnit;

    private String cronExpression;

    private SupportedTimezone timezone;

    private Integer maxRetryCount;

    private String payload;
}