package com.airtribe.jobschedular.repository;
import com.airtribe.jobschedular.entity.TaskExecutionHistory;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.List;
import java.util.UUID;

public interface TaskExecutionHistoryRepository extends CassandraRepository<TaskExecutionHistory, TaskExecutionHistory.Key> {

    List<TaskExecutionHistory> findByKeyJobId(UUID jobId);
}