package com.budgetbuddy.analytics_service.model;


public class BudgetUsage {
    private String category;
    private double limit;
    private double spent;
    private double remaining;
    private double percentUsed;

    public BudgetUsage(String category, double limit, double spent, double remaining, double percentUsed) {
        this.category = category;
        this.limit = limit;
        this.spent = spent;
        this.remaining = remaining;
        this.percentUsed = percentUsed;
    }

    // Getters and setters

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getLimit() {
        return limit;
    }

    public void setLimit(double limit) {
        this.limit = limit;
    }

    public double getSpent() {
        return spent;
    }

    public void setSpent(double spent) {
        this.spent = spent;
    }

    public double getRemaining() {
        return remaining;
    }

    public void setRemaining(double remaining) {
        this.remaining = remaining;
    }

    public double getPercentUsed() {
        return percentUsed;
    }

    public void setPercentUsed(double percentUsed) {
        this.percentUsed = percentUsed;
    }
}
