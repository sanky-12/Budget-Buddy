package com.budgetbuddy.analytics_service.controller;


import com.budgetbuddy.analytics_service.model.AnalyticsResponse;
import com.budgetbuddy.analytics_service.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;


    @Operation(
            summary     = "Get analytics summary",
            description = "Returns spending and budgeting summary for the authenticated user. " +
                    "If `monthYear` is provided (format `yyyy-MM`), filters data to that month.",
            responses   = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AnalyticsResponse.class)))
            }
    )
    @GetMapping("/summary")
    public AnalyticsResponse getAnalyticsSummary(
            @Parameter(
                    description = "Optional month filter in format yyyy-MM",
                    required    = false,
                    example     = "2025-07"
            )
            @RequestParam(required = false) String monthYear) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return analyticsService.getUserAnalytics(userId, monthYear);
    }


    @Operation(
            summary     = "Get available months",
            description = "Returns a list of month-year strings (`yyyy-MM`) for which the user has data.",
            responses   = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = String.class, type = "array")))
            }
    )
    @GetMapping("/available-months")
    public List<String> getAvailableMonths() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return analyticsService.getAvailableMonths(userId);
    }

}
