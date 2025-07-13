package com.budgetbuddy.budget_service.controller;


import com.budgetbuddy.budget_service.model.Budget;
import com.budgetbuddy.budget_service.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/budgets")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;


    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Budget> createBudgets(@RequestBody List<Budget> budgets) {
        String userId = getCurrentUserId();
        budgets.forEach(b -> b.setUserId(userId));
        return budgetService.addBudgets(budgets);
    }

    @PostMapping("/copy")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Budget> copyPreviousMonthBudget(@RequestParam String from, @RequestParam String to) {
        String userId = getCurrentUserId();
        return budgetService.copyBudgets(userId, from, to);
    }



    @GetMapping
    public List<Budget> getBudgets(@RequestParam(required = false) String monthYear) {
        String userId = getCurrentUserId();
        if (monthYear != null && !monthYear.isEmpty()) {
            return budgetService.getBudgetsByUserAndMonth(userId, monthYear);
        }
        return budgetService.getBudgetsByUser(userId);
    }


    @GetMapping("/category")
    public Optional<Budget> getBudgetByCategoryAndMonth(@RequestParam String category, @RequestParam String monthYear) {
        return budgetService.getBudget(getCurrentUserId(), category, monthYear);
    }

    @PutMapping("/{id}")
    public Budget updateBudget(@PathVariable String id, @RequestBody Budget budget) {
        budget.setUserId(getCurrentUserId());
        return budgetService.updateBudget(id, budget);
    }


    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}