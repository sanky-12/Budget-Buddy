package com.budgetbuddy.expense_service.controller;

import com.budgetbuddy.expense_service.model.Expense;
import com.budgetbuddy.expense_service.service.ExpenseService;
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

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Expense createExpense(@Valid @RequestBody Expense expense){
        expense.setUserId(getUserIdFromContext());
        return expenseService.addExpense(expense);
    }

    @GetMapping("/{id}")
    public Optional<Expense> getExpenseById(@PathVariable String id){
        return expenseService.getExpenseById(id);
    }


    @GetMapping
    public List<Expense> filterExpenses(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) throws ParseException {
        String userId = getUserIdFromContext();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date start = (startDate != null) ? formatter.parse(startDate) : null;
        Date end = (endDate != null) ? formatter.parse(endDate) : null;

        return expenseService.filterExpenses(userId, category, start, end);
    }


    @PutMapping("/{id}")
    public Expense  updateExpense(@PathVariable String id, @RequestBody Expense expenseDetails){
        return  expenseService.updateExpense(id, expenseDetails);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExpense(@PathVariable String id){
        expenseService.deleteExpense(id);
    }

    private String getUserIdFromContext() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

}
