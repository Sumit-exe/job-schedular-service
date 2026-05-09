package com.airtribe.jobschedular.repository;
import com.airtribe.jobschedular.entity.TaskSchedule;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import java.util.List;
import java.util.UUID;

public interface TaskScheduleRepository extends CassandraRepository<TaskSchedule, TaskSchedule.Key> {

    List<TaskSchedule> findByKeyExecutionBucket(String executionBucket);

    @Query("UPDATE task_schedule SET status = 'QUEUED' " +
            "WHERE execution_bucket = ?0 AND job_id = ?1 IF status = 'SCHEDULED'")
    boolean markAsQueued(String bucket, UUID jobId);
}