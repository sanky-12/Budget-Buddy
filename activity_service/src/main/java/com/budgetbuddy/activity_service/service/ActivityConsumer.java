package com.budgetbuddy.activity_service.service;

import com.budgetbuddy.activity_service.model.ActivityLog;
import com.budgetbuddy.activity_service.repository.ActivityLogRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import com.budgetbuddy.events.ActivityEvent;

@Service
public class ActivityConsumer {
    private final ActivityLogRepository repo;

    public ActivityConsumer(ActivityLogRepository r) { this.repo = r; }

    @KafkaListener(topics="user-activity-logs", groupId="activity-service")
    public void listen(ActivityEvent evt) {
        // Convert to your Mongo-backed domain if needed
        // ActivityLog l = new ActivityLog();
        ActivityLog log = new ActivityLog(
                evt.getTimestamp(),
                evt.getUserId(),
                evt.getAction(),
                evt.getEntityType(),
                evt.getEntityId()
        );
        repo.save(log);
        System.out.println("Saved: " + log);
    }
}