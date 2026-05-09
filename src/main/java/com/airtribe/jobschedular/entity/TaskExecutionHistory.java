package com.airtribe.jobschedular.entity;

import com.airtribe.jobschedular.util.enums.ExecutionStatus;
import lombok.Data;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;

import java.time.Instant;
import java.util.UUID;

@Data
@Table("task_execution_history")
public class TaskExecutionHistory {

    @PrimaryKeyClass
    @Data
    public static class Key {

        @PrimaryKeyColumn(name = "job_id", type = PrimaryKeyType.PARTITIONED)
        private UUID jobId;

        @PrimaryKeyColumn(name = "execution_time", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
        private Instant executionTime;
    }

    @PrimaryKey
    private Key key;

    @Column("status")
    private ExecutionStatus status;

    @Column("retry_count")
    private Integer retryCount;

    @Column("last_update_time")
    private Instant lastUpdateTime;

    @Column("error_message")
    private String errorMessage;

}
