package com.budgetbuddy.income_service.controller;

import com.budgetbuddy.income_service.model.Income;
import com.budgetbuddy.income_service.service.IncomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/income")
public class IncomeController {

    @Autowired
    private IncomeService incomeService;


    @Operation(
            summary = "Register a new income record",
            description = "Adds an income entry for the authenticated user",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Income.class)
                            )
                    )
            }
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Income addIncome(
            @Parameter(description = "Income payload", required = true)
            @Valid @RequestBody Income income
    ) {
        income.setUserId(getCurrentUserId());
        return incomeService.addIncome(income);
    }


    @Operation(
            summary = "List income records",
            description = "Fetches all income or filters by date range if startDate & endDate are provided",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Income.class, type = "array")
                            )
                    )
            }
    )
    @GetMapping
    public List<Income> getIncome(
            @Parameter(description = "Start date (yyyy-MM-dd)", required = false)
            @RequestParam(required = false) String startDate,
            @Parameter(description = "End date (yyyy-MM-dd)", required = false)
            @RequestParam(required = false) String endDate
    ) throws ParseException {
        String userId = getCurrentUserId();
        // both dates provided → filter by range
        if (startDate != null && endDate != null) {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
            Date start = fmt.parse(startDate);
            Date end   = fmt.parse(endDate);
            return incomeService.getIncomeByDateRange(userId, start, end);
        }
        // otherwise return all
        return incomeService.getAllIncome(userId);
    }

    @Operation(
            summary = "Get a single income record by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Income.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Not found")
            }
    )
    @GetMapping("/{id}")
    public Optional<Income> getIncomeById(
            @Parameter(description = "ID of income", required = true)
            @PathVariable String id
    ) {
        return incomeService.getIncomeById(id);
    }

    @Operation(
            summary = "Update an existing income entry",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Updated",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Income.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Not found")
            }
    )
    @PutMapping("/{id}")
    public Income updateIncome(
            @Parameter(description = "ID of income to update", required = true)
            @PathVariable String id,
            @Parameter(description = "Updated income payload", required = true)
            @RequestBody Income income) {
        income.setUserId(getCurrentUserId());
        return incomeService.updateIncome(id, income);
    }

    @Operation(
            summary = "Delete an income record",
            responses = {
                    @ApiResponse(responseCode = "204", description = "No Content"),
                    @ApiResponse(responseCode = "404", description = "Not found")
            }
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteIncome(
            @Parameter(description = "ID of income to delete", required = true)
            @PathVariable String id) {
        incomeService.deleteIncome(id);
    }



    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}