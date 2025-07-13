package com.budgetbuddy.budget_service.repository;

import com.budgetbuddy.budget_service.model.Budget;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends MongoRepository<Budget, String> {
    List<Budget> findByUserId(String userId);
    Optional<Budget> findByUserIdAndCategoryAndMonthYear(String userId, String category, String monthYear);
    List<Budget> findByUserIdAndMonthYear(String userId, String monthYear);
}