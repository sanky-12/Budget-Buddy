package com.budgetbuddy.activity_service.model;

import com.budgetbuddy.activity_service.util.JsonUtil;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "activity_logs")
public class ActivityLog {
    @Id
    private String id;
    private Instant timestamp;
    private String userId;
    private String action;
    private String entityType;
    private String entityId;

    public ActivityLog(Instant timestamp, String userId,
                       String action, String entityType, String entityId) {
        this.timestamp  = timestamp;
        this.userId     = userId;
        this.action     = action;
        this.entityType = entityType;
        this.entityId   = entityId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public static ActivityLog fromJson(String json) {
        return JsonUtil.fromJson(json, ActivityLog.class);
    }
}