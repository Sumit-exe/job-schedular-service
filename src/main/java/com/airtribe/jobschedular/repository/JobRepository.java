package com.airtribe.jobschedular.repository;

import com.airtribe.jobschedular.entity.Job;
import org.springframework.data.cassandra.repository.AllowFiltering;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobRepository extends CassandraRepository<Job, Job.Key> {

    List<Job> findByKeyUserId(UUID userId);
    Optional<Job> findByKeyUserIdAndKeyJobId(UUID userId, UUID jobId);
    @AllowFiltering
    Optional<Job> findByKeyJobId(UUID jobId);
}