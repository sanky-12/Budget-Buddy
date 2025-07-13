package com.budgetbuddy.expense_service.repository;

import com.budgetbuddy.expense_service.model.Expense;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

public interface ExpenseRepository extends MongoRepository<Expense, String> {
    List<Expense> findByUserId(String userId);
    List<Expense> findByUserIdAndCategory(String userId, String category);
    List<Expense> findByUserIdAndDateBetween(String userId, Date startDate, Date endDate);
    List<Expense> findByUserIdAndDateAfter(String userId, Date startDate);
    List<Expense> findByUserIdAndDateBefore(String userId, Date endDate);
    List<Expense> findByUserIdAndCategoryAndDateBetween(String userId, String category, Date start, Date end);
    List<Expense> findByUserIdAndCategoryAndDateAfter(String userId, String category, Date start);
    List<Expense> findByUserIdAndCategoryAndDateBefore(String userId, String category, Date end);

}
