package com.airtribe.jobschedular.entity;

import com.airtribe.jobschedular.util.enums.ExecutionStatus;
import lombok.Data;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;
import java.time.Instant;
import java.util.UUID;

@Data
@Table("task_schedule")
public class TaskSchedule {

    @PrimaryKeyClass
    @Data
    public static class Key {

        @PrimaryKeyColumn(name = "execution_bucket", type = PrimaryKeyType.PARTITIONED)
        private String executionBucket;

        @PrimaryKeyColumn(name = "job_id", type = PrimaryKeyType.CLUSTERED)
        private UUID jobId;
    }

    @PrimaryKey
    private Key key;

    @Column("next_execution_time")
    private Instant nextExecutionTime;

    @Column("status")
    private ExecutionStatus status;

    @Column("payload")
    private String payload;

    @Column("is_active")
    private Boolean isActive;
}