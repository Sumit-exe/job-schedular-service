package com.airtribe.jobschedular.schedular;

import com.airtribe.jobschedular.dto.JobEvent;
import com.airtribe.jobschedular.entity.TaskSchedule;
import com.airtribe.jobschedular.kafka.producer.JobProducer;
import com.airtribe.jobschedular.repository.TaskScheduleRepository;
import com.airtribe.jobschedular.util.BucketUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobSchedulerService {

    private final TaskScheduleRepository repo;
    private final JobProducer producer;

    @Scheduled(fixedDelay = 60000)
    public void run() {
        String bucket =
                BucketUtil.getCurrentBucket();
        log.info("schdular running for bucket: {}", bucket);

        List<TaskSchedule> schedules =
                repo.findByKeyExecutionBucket(bucket);
        log.info("List<TaskSchedule> schedules: {}", schedules);

        for (TaskSchedule schedule : schedules) {

            try {

                if (!Boolean.TRUE.equals(
                        schedule.getIsActive()
                )) {
                    continue;
                }

                boolean locked =
                        repo.markAsQueued(
                                bucket,
                                schedule.getKey().getJobId()
                        );

                if (!locked) {
                    continue;
                }

                JobEvent event = new JobEvent(
                        schedule.getKey().getJobId().toString(),
                        schedule.getPayload(),
                        0
                );

                producer.send(event);

            } catch (Exception ex) {

                log.error(
                        "Scheduler failed for job: {}",
                        schedule.getKey().getJobId(),
                        ex
                );
            }
        }
    }
}