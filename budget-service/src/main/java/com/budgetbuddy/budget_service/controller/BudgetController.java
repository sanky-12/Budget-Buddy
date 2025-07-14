package com.budgetbuddy.budget_service.controller;


import com.budgetbuddy.budget_service.model.Budget;
import com.budgetbuddy.budget_service.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/budgets")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @Operation(
            summary     = "Bulk create budgets",
            description = "Add multiple budget entries for the authenticated user in one call",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Budgets created",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Budget.class, type = "array"))),
                    @ApiResponse(responseCode = "400", description = "Validation error")
            }
    )
    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Budget> createBudgets(
            @Parameter(description = "List of budgets to create", required = true)
            @RequestBody List<Budget> budgets
    ) {
        String userId = getCurrentUserId();
        budgets.forEach(b -> b.setUserId(userId));
        return budgetService.addBudgets(budgets);
    }

    @Operation(
            summary     = "Copy budgets from one month to another",
            description = "Clone all budgets from `from` monthYear into `to` monthYear (fails if `to` already exists)",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Budgets copied",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Budget.class, type = "array"))),
                    @ApiResponse(responseCode = "409", description = "Target month already has budgets")
            }
    )
    @PostMapping("/copy")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Budget> copyPreviousMonthBudget(
            @Parameter(description = "Source monthYear (yyyy-MM)", required = true)
            @RequestParam String from,
            @Parameter(description = "Destination monthYear (yyyy-MM)", required = true)
            @RequestParam String to
    ) {
        String userId = getCurrentUserId();
        return budgetService.copyBudgets(userId, from, to);
    }


    @Operation(
            summary     = "List budgets",
            description = "Get all budgets for the authenticated user, optionally filtered by monthYear",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Budget.class, type = "array")))
            }
    )
    @GetMapping
    public List<Budget> getBudgets(
            @Parameter(description = "Optional monthYear filter (yyyy-MM)", required = false)
            @RequestParam(required = false) String monthYear
    ) {
        String userId = getCurrentUserId();
        if (monthYear != null && !monthYear.isEmpty()) {
            return budgetService.getBudgetsByUserAndMonth(userId, monthYear);
        }
        return budgetService.getBudgetsByUser(userId);
    }


    @Operation(
            summary     = "Get a budget by category and month",
            description = "Retrieve the single budget entry matching category + monthYear",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Found",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Budget.class))),
                    @ApiResponse(responseCode = "404", description = "Not found")
            }
    )
    @GetMapping("/category")
    public Optional<Budget> getBudgetByCategoryAndMonth(
            @Parameter(description = "Budget category", required = true)
            @RequestParam String category,
            @Parameter(description = "monthYear (yyyy-MM)", required = true)
            @RequestParam String monthYear
    ) {
        return budgetService.getBudget(getCurrentUserId(), category, monthYear);
    }


    @Operation(
            summary     = "Update a budget entry",
            description = "Modify limitAmount (or other fields) of an existing budget",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Updated",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Budget.class))),
                    @ApiResponse(responseCode = "404", description = "Not found")
            }
    )
    @PutMapping("/{id}")
    public Budget updateBudget(
            @Parameter(description = "ID of budget to update", required = true)
            @PathVariable String id,
            @Parameter(description = "Updated budget payload", required = true)
            @RequestBody Budget budget
    ) {
        budget.setUserId(getCurrentUserId());
        return budgetService.updateBudget(id, budget);
    }


    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}