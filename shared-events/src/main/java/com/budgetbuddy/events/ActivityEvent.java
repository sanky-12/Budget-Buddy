package com.budgetbuddy.events;

import java.time.Instant;

public class ActivityEvent {
    private String userId;
    private String action;
    private String entityType;    // ← new
    private String entityId;
    private Instant timestamp;

    // No-arg constructor for Jackson
    public ActivityEvent() {}

    public ActivityEvent(
            String userId,
            String action,
            String entityType,       // ← include here
            String entityId,
            Instant timestamp
    ) {
        this.userId     = userId;
        this.action     = action;
        this.entityType = entityType;
        this.entityId   = entityId;
        this.timestamp  = timestamp;
    }

    // Getters & setters
    public String getUserId()         { return userId; }
    public void setUserId(String u)   { this.userId = u; }

    public String getAction()         { return action; }
    public void setAction(String a)   { this.action = a; }

    public String getEntityType()         { return entityType; }
    public void setEntityType(String t)   { this.entityType = t; }

    public String getEntityId()       { return entityId; }
    public void setEntityId(String e) { this.entityId = e; }

    public Instant getTimestamp()     { return timestamp; }
    public void setTimestamp(Instant t) { this.timestamp = t; }
}
