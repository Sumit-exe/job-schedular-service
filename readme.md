# Job Scheduler Platform

A distributed job scheduling and execution platform built using Java, Spring Boot, Kafka, Cassandra, and React.

The platform allows users to:

- Create and manage scheduled jobs
- Execute recurring and one-time tasks
- Process jobs asynchronously using Kafka
- Track execution history and retries
- Manage authentication and authorization
- Scale job execution independently

---

# System Architecture

## High Level Architecture

```text
                    +----------------------+
                    |   React Frontend     |
                    |  (Vite + React)      |
                    +----------+-----------+
                               |
                               v
                  +------------+-------------+
                  |     Auth Service         |
                  |  JWT Authentication      |
                  +------------+-------------+
                               |
                               v
                  +------------+-------------+
                  | Scheduler Service        |
                  | Create / Manage Jobs     |
                  +------------+-------------+
                               |
                               v
                        Apache Kafka
                  (job-execution-topic)
                               |
                               v
                  +------------+-------------+
                  | Executor Service         |
                  | Execute Scheduled Jobs   |
                  +------------+-------------+
                               |
                               v
                         Cassandra DB
```

---

# Tech Stack

## Backend

- Java 17
- Spring Boot
- Spring WebFlux
- Spring Security
- Spring Data Cassandra
- Apache Kafka
- JWT Authentication
- Maven

## Database

- Apache Cassandra

## Messaging

- Apache Kafka
- Zookeeper

## Frontend

- React
- Vite
- Axios
- Tailwind CSS

## DevOps / Infra

- Docker / Podman
- Docker Compose

---

# Microservices

---

# 1. Job Scheduler Service

Repository:

[job-schedular-service](https://github.com/Sumit-exe/job-schedular-service?utm_source=chatgpt.com)

## Responsibilities

- Create jobs
- Schedule recurring jobs
- Store job metadata
- Push executable jobs to Kafka
- Maintain scheduling buckets
- Handle cron-like execution logic

## Core Features

- One-time jobs
- Recurring jobs
- Retry support
- Bucket-based scheduling
- Timezone-aware execution
- Cassandra-based persistence
- Kafka producer integration

## Main Components

### Controllers

Expose REST APIs for:

- Creating jobs
- Updating jobs
- Fetching jobs
- Activating/deactivating jobs

### Scheduler Engine

Responsible for:

- Polling execution buckets
- Identifying due jobs
- Publishing jobs to Kafka

### Cassandra Tables

- jobs
- task_schedule
- task_execution_history

---

# 2. Job Executor Service

Repository:

[job-executor-service](https://github.com/Sumit-exe/job-executor-service?utm_source=chatgpt.com)

## Responsibilities

- Consume Kafka messages
- Execute jobs
- Handle retries
- Maintain execution history
- Execute handlers dynamically

## Core Features

- Kafka consumer
- Retry mechanism
- Failure tracking
- Dynamic handler factory
- Execution history storage
- Rescheduling support

## Flow

```text
Kafka Message
      ↓
Executor Consumer
      ↓
Job Handler Factory
      ↓
Specific Handler Execution
      ↓
Execution History Saved
      ↓
Retry or Reschedule
```

## Retry Logic

- Failed jobs retry automatically
- Configurable retry count
- Delayed retry scheduling
- Failure reason persistence

---

# 3. Job Auth Service

Repository:

[job-auth-service](https://github.com/Sumit-exe/job-auth-service?utm_source=chatgpt.com)

## Responsibilities

- User authentication
- JWT token generation
- User management
- Authorization

## Core Features

- Signup/Login
- JWT authentication
- Secure API access
- Password encryption
- Cassandra user storage

## Security

- Stateless JWT authentication
- Spring Security integration
- BCrypt password hashing

---

# Frontend

Repository:

[job-schedular-frontend](https://github.com/Sumit-exe/job-schedular-frontend?utm_source=chatgpt.com)

## Features

- User authentication
- Dashboard
- Create/manage jobs
- View execution history
- Real-time status visibility
- Responsive UI

---

# Database Design

## Cassandra Tables

---

## users

```sql
CREATE TABLE users (
    userid UUID PRIMARY KEY,
    email TEXT,
    password TEXT,
    createdat TIMESTAMP
);
```

---

## jobs

Stores master job metadata.

Fields include:

- jobId
- cronExpression
- timezone
- payload
- retryCount
- status

---

## task_schedule

Stores upcoming executions.

Partitioned using execution buckets.

Fields include:

- executionBucket
- jobId
- nextExecutionTime
- payload
- status

---

## task_execution_history

Stores execution history.

Fields include:

- jobId
- executionTime
- retryCount
- status
- errorMessage

---

# Scheduling Design

## Bucket-Based Scheduling

Instead of scanning all jobs continuously, jobs are grouped into time buckets.

Example:

```text
2026-05-09-19-30
2026-05-09-19-31
2026-05-09-19-32
```

Benefits:

- Faster querying
- Reduced DB scans
- Better scalability
- Efficient partitioning

---

# Kafka Integration

## Topic

```text
job-execution-topic
```

## Producer

Scheduler Service publishes executable jobs.

## Consumer

Executor Service consumes and executes jobs.

## Benefits

- Loose coupling
- Horizontal scalability
- Async processing
- Fault tolerance

---

# Execution Flow

```text
User Creates Job
        ↓
Scheduler Stores Job
        ↓
Job Added To Execution Bucket
        ↓
Scheduler Polls Current Bucket
        ↓
Publishes Event To Kafka
        ↓
Executor Consumes Event
        ↓
Handler Executes Task
        ↓
Execution History Saved
        ↓
Retry / Reschedule
```

---

# Best Features of the Project

## Distributed Architecture

Each service is independently scalable.

---

## Event-Driven Design

Kafka-based asynchronous execution.

---

## Cassandra-Based Scheduling

Highly scalable scheduling system using bucket partitioning.

---

## Retry Mechanism

Automatic retries for failed jobs.

---

## Timezone-Aware Scheduling

Jobs can execute based on user timezone.

---

## Extensible Job Handlers

Easy to add new job types using handler factory pattern.

---

## Scalable Executor Design

Multiple executor instances can consume jobs simultaneously.

---

# APIs

---

# Auth Service APIs

## Signup

```http
POST /auth/signup
```

Request:

```json
{
  "email": "test@gmail.com",
  "password": "password"
}
```

---

## Login

```http
POST /auth/login
```

Response:

```json
{
  "token": "JWT_TOKEN"
}
```

---

# Scheduler APIs

## Create Job

```http
POST /jobs
```

Request:

```json
{
  "name": "Sample Job",
  "cronExpression": "0 */1 * * * *",
  "timezone": "Asia/Kolkata",
  "payload": {}
}
```

---

## Get Jobs

```http
GET /jobs
```

---

## Update Job

```http
PUT /jobs/{id}
```

---

## Delete Job

```http
DELETE /jobs/{id}
```

---

# Local Development Setup

# Prerequisites

Install:

- Java JDK 17
- Maven
- Node.js
- Docker or Podman

---

# Clone Repositories

## Backend

```bash
git clone https://github.com/Sumit-exe/job-schedular-service
git clone https://github.com/Sumit-exe/job-executor-service
git clone https://github.com/Sumit-exe/job-auth-service
```

---

## Frontend

```bash
git clone https://github.com/Sumit-exe/job-schedular-frontend
```

---

# Build Backend Services

Run inside each backend service:

```bash
mvn clean install
```

---

# Install Frontend Dependencies

```bash
npm install
```

---

# Setup Cassandra & Kafka Using Podman

## Install Podman

Initialize Podman machine:

```bash
podman machine init
podman machine start
```

---

# Start Containers

Go to directory containing `docker-compose.yaml`

```bash
podman-compose up -d
```

Check running containers:

```bash
podman ps
```

Ensure these containers are running:

- Cassandra
- Kafka
- Zookeeper

---

# Setup Cassandra Keyspace

Open Cassandra shell:

```bash
podman exec -it job-cassandra cqlsh
```

Create keyspace:

```sql
CREATE KEYSPACE scheduler
WITH replication = {
  'class': 'SimpleStrategy',
  'replication_factor': 1
};
```

Verify:

```sql
DESCRIBE KEYSPACES;
USE scheduler;
```

---

# Start Backend Services

Run all microservices.

Verify tables:

```sql
DESCRIBE TABLES;
SELECT * FROM users;
```

---

# Start Frontend

```bash
npm run dev
```

---

# Create Kafka Topic

Open Kafka container:

```bash
podman exec -it job-kafka bash
```

List topics:

```bash
/usr/bin/kafka-topics --bootstrap-server localhost:9092 --list
```

Create topic:

```bash
kafka-topics \
--bootstrap-server localhost:9092 \
--create \
--topic job-execution-topic \
--partitions 1 \
--replication-factor 1
```

Consume messages:

```bash
kafka-console-consumer \
--bootstrap-server localhost:9092 \
--topic job-execution-topic \
--from-beginning
```

---

# Future Improvements

- Cron expression builder UI
- Dead letter queue
- Job priority support
- Distributed locking
- Metrics & monitoring
- Grafana dashboards
- Kubernetes deployment
- Multi-tenant support
- Notification system
- Email/SMS/Webhook jobs

---

# Scalability Considerations

## Horizontal Scaling

- Multiple scheduler instances
- Multiple executor instances
- Kafka partition scalability

## Cassandra Advantages

- High write throughput
- Partition scalability
- Fault tolerance

---

# Design Patterns Used

- Factory Pattern
- Repository Pattern
- Dependency Injection
- Event-Driven Architecture
- Microservices Architecture

---

# Author

Sumit Sharma

GitHub:

[Sumit-exe GitHub](https://github.com/Sumit-exe)