package com.airtribe.jobschedular.kafka.producer;

import com.airtribe.jobschedular.dto.JobEvent;
import com.airtribe.jobschedular.entity.TaskSchedule;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void send(JobEvent event) {
        try {
            String msg = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("job-execution-topic", event.getJobId(), msg);
            log.info("msg sent to job-execution-topic : {}",msg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}