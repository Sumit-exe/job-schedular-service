package com.airtribe.jobschedular.entity;

import com.airtribe.jobschedular.util.enums.ExecutionStatus;
import com.airtribe.jobschedular.util.enums.ScheduleType;
import com.airtribe.jobschedular.util.enums.SupportedTimezone;
import com.airtribe.jobschedular.util.enums.TimeUnit;
import lombok.Data;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;
import java.time.Instant;
import java.util.UUID;

@Data
@Table("job")
public class Job {

    @PrimaryKeyClass
    @Data
    public static class Key {

        @PrimaryKeyColumn(name = "user_id", type = PrimaryKeyType.PARTITIONED)
        private UUID userId;

        @PrimaryKeyColumn(name = "job_id", type = PrimaryKeyType.CLUSTERED)
        private UUID jobId;
    }

    @PrimaryKey
    private Key key;

    @Column("is_recurring")
    private Boolean isRecurring;

    @Column("interval")
    private String interval;

    @Column("max_retry_count")
    private Integer maxRetryCount;

    @Column("created_time")
    private Instant createdTime;

    @Column("payload")
    private String payload;

    @Column("execution_status")
    private ExecutionStatus executionStatus;

    @Column("is_active")
    private Boolean isActive;

    private ScheduleType scheduleType;

    private Instant executeAt;

    private Integer frequency;

    private TimeUnit timeUnit;

    private String cronExpression;

    @Column("timezone")
    private SupportedTimezone timezone;

}