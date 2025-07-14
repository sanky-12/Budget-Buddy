package com.budgetbuddy.expense_service.controller;

import com.budgetbuddy.expense_service.model.Expense;
import com.budgetbuddy.expense_service.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @Operation(
            summary = "Create a new Expense",
            description = "Save an Expense for the authenticated user",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created",
                        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = Expense.class))),
                    @ApiResponse(responseCode = "400", description = "Validation error")
            }
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Expense createExpense(
            @Parameter(description = "Expense payload", required = true)
            @Valid @RequestBody Expense expense
    ){
        expense.setUserId(getUserIdFromContext());
        return expenseService.addExpense(expense);
    }

    @Operation(
            summary = "Get an expense by ID",
            description = "Retrieve a single expense belonging to the authenticated user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Found",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = Expense.class))),
                    @ApiResponse(responseCode = "404", description = "Not found")
            }
    )
    @GetMapping("/{id}")
    public Optional<Expense> getExpenseById(
            @Parameter(description = "Id of the expense", required = true)
            @PathVariable String id
    ){
        return expenseService.getExpenseById(id);
    }


    @Operation(
            summary = "Filter expenses",
            description = "List all expenses with optional category and/or date-range filters",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = Expense.class, type = "array")))
            }
    )
    @GetMapping
    public List<Expense> filterExpenses(
            @Parameter(description = "Expense category to filter by", required = false)
            @RequestParam(required = false) String category,
            @Parameter(description = "Start date (yyyy-MM-dd)", required = false)
            @RequestParam(required = false) String startDate,
            @Parameter(description = "End date (yyyy-MM-dd)", required = false)
            @RequestParam(required = false) String endDate
    ) throws ParseException {
        String userId = getUserIdFromContext();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date start = (startDate != null) ? formatter.parse(startDate) : null;
        Date end = (endDate != null) ? formatter.parse(endDate) : null;

        return expenseService.filterExpenses(userId, category, start, end);
    }


    @Operation(
            summary = "Update an expense",
            description = "Modify an existing expense",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Updated",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = Expense.class))),
                    @ApiResponse(responseCode = "404", description = "Not found")
            }
    )
    @PutMapping("/{id}")
    public Expense  updateExpense(
            @Parameter(description = "ID of the expense to update", required = true)
            @PathVariable String id,
            @Parameter(description = "Updated expense payload", required = true)
            @RequestBody Expense expenseDetails
    ){
        return  expenseService.updateExpense(id, expenseDetails);
    }


    @Operation(
            summary = "Delete an expense",
            description = "Remove an expense by its ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "No Content"),
                    @ApiResponse(responseCode = "404", description = "Not found")
            }
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExpense(
            @Parameter(description = "ID of the expense to delete", required = true)
            @PathVariable String id){
        expenseService.deleteExpense(id);
    }

    private String getUserIdFromContext() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

}
