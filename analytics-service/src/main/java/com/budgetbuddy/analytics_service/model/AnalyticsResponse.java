package com.budgetbuddy.analytics_service.model;

import java.util.List;

public class AnalyticsResponse {
    private double totalIncome;
    private double totalExpenses;
    private double netSavings;
    private List<BudgetUsage> budgetUsage;

    // Getters and setters

    public double getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(double totalIncome) {
        this.totalIncome = totalIncome;
    }

    public double getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(double totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public double getNetSavings() {
        return netSavings;
    }

    public void setNetSavings(double netSavings) {
        this.netSavings = netSavings;
    }

    public List<BudgetUsage> getBudgetUsage() {
        return budgetUsage;
    }

    public void setBudgetUsage(List<BudgetUsage> budgetUsage) {
        this.budgetUsage = budgetUsage;
    }
}
