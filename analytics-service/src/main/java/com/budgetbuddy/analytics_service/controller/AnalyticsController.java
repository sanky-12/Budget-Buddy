package com.budgetbuddy.analytics_service.controller;


import com.budgetbuddy.analytics_service.model.AnalyticsResponse;
import com.budgetbuddy.analytics_service.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

//    @GetMapping("/summary")
//    public AnalyticsResponse getAnalyticsSummary() {
//        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
//        return analyticsService.getUserAnalytics(userId);
//    }

    @GetMapping("/summary")
    public AnalyticsResponse getAnalyticsSummary(@RequestParam(required = false) String monthYear) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return analyticsService.getUserAnalytics(userId, monthYear);
    }

    @GetMapping("/available-months")
    public List<String> getAvailableMonths() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return analyticsService.getAvailableMonths(userId);
    }

}
