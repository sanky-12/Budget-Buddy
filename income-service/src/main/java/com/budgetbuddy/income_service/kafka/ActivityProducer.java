package com.budgetbuddy.income_service.kafka;

import org.springframework.stereotype.Service;
import com.budgetbuddy.events.ActivityEvent;
import org.springframework.kafka.core.KafkaTemplate;

@Service
public class ActivityProducer {
    private final KafkaTemplate<String, ActivityEvent> kafka;
    public ActivityProducer(KafkaTemplate<String, ActivityEvent> k) {
        this.kafka = k;
    }

    public void send(ActivityEvent evt) {
        kafka.send("user-activity-logs", evt.getEntityId(), evt);
        System.out.println("✅ Sent event to Kafka: " + evt);
    }
}
