package com.budgetbuddy.analytics_service.service;


import com.budgetbuddy.analytics_service.client.BudgetClient;
import com.budgetbuddy.analytics_service.client.ExpenseClient;
import com.budgetbuddy.analytics_service.client.IncomeClient;
import com.budgetbuddy.analytics_service.model.AnalyticsResponse;
import com.budgetbuddy.analytics_service.model.Budget;
import com.budgetbuddy.analytics_service.model.BudgetUsage;
import com.budgetbuddy.analytics_service.model.Expense;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private ExpenseClient expenseClient;
    @Autowired private IncomeClient incomeClient;
    @Autowired private BudgetClient budgetClient;


    public AnalyticsResponse getUserAnalytics(String userId, String monthYear) {
        List<Expense> expenses = expenseClient.getExpenses(userId);
        List<Budget> budgets = budgetClient.getBudgets(userId);

        // Filter by selected month if provided
        if (monthYear != null && !monthYear.isEmpty()) {
            expenses = expenses.stream()
                    .filter(e -> e.getDate().toInstant().toString().startsWith(monthYear))
                    .collect(Collectors.toList());

            budgets = budgets.stream()
                    .filter(b -> monthYear.equals(b.getMonthYear()))
                    .collect(Collectors.toList());
        }

        // Group expenses by category
        Map<String, Double> spentByCategory = expenses.stream()
                .collect(Collectors.groupingBy(Expense::getCategory, Collectors.summingDouble(Expense::getAmount)));

        // Group budgets by category and sum limit amounts
        Map<String, Double> limitByCategory = budgets.stream()
                .collect(Collectors.groupingBy(Budget::getCategory, Collectors.summingDouble(Budget::getLimitAmount)));

        List<BudgetUsage> usageList = new ArrayList<>();
        for (String category : limitByCategory.keySet()) {
            double limit = limitByCategory.getOrDefault(category, 0.0);
            double spent = spentByCategory.getOrDefault(category, 0.0);
            double remaining = limit - spent;
            double percentUsed = limit == 0 ? 0.0 : (spent / limit) * 100.0;

            usageList.add(new BudgetUsage(category, limit, spent, remaining, percentUsed));
        }

        double totalExpenses = expenses.stream().mapToDouble(Expense::getAmount).sum();
        double totalIncome = incomeClient.getTotalIncome(userId); // optional: filter income too
        double netSavings = totalIncome - totalExpenses;

        AnalyticsResponse response = new AnalyticsResponse();
        response.setTotalExpenses(totalExpenses);
        response.setTotalIncome(totalIncome);
        response.setNetSavings(netSavings);
        response.setBudgetUsage(usageList);

        return response;
    }


    public List<String> getAvailableMonths(String userId) {
        List<Budget> budgets = budgetClient.getBudgets(userId);
        return budgets.stream()
                .map(Budget::getMonthYear)
                .filter(month -> month != null && !month.isEmpty())
                .distinct()
                .sorted(Comparator.reverseOrder()) // Optional: newest first
                .collect(Collectors.toList());
    }

}
