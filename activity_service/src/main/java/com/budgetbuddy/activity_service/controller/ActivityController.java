package com.budgetbuddy.activity_service.controller;

import com.budgetbuddy.activity_service.model.ActivityLog;
import com.budgetbuddy.activity_service.repository.ActivityLogRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/activity")
public class ActivityController {
    private final ActivityLogRepository repo;

    public ActivityController(ActivityLogRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/logs")
    public List<ActivityLog> getAll() {
        return repo.findAll();
    }

    @GetMapping("/logs/user/{userId}")
    public List<ActivityLog> byUser(@PathVariable String userId) {
        return repo.findByUserId(userId);
    }

    @GetMapping("/logs/type/{entityType}")
    public List<ActivityLog> byType(@PathVariable String entityType) {
        return repo.findByEntityType(entityType);
    }

    @GetMapping("/logs/range")
    public List<ActivityLog> byDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return repo.findByTimestampBetween(from, to);
    }
}
