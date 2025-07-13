package com.budgetbuddy.activity_service.repository;

import com.budgetbuddy.activity_service.model.ActivityLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ActivityLogRepository extends MongoRepository<ActivityLog, String> {
    List<ActivityLog> findByUserId(String userId);
    List<ActivityLog> findByTimestampBetween(Instant start, Instant end);
    List<ActivityLog> findByEntityType(String entityType);
}
