package com.budgetbuddy.expense_service.service;

import com.budgetbuddy.events.ActivityEvent;
import com.budgetbuddy.expense_service.kafka.ActivityProducer;
import com.budgetbuddy.expense_service.model.Expense;
import com.budgetbuddy.expense_service.repository.ExpenseRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ActivityProducer producer;
    private static final String TOPIC = "user-activity-logs";

    public ExpenseService(ExpenseRepository repository,
                          ActivityProducer producer) {
        this.expenseRepository = repository;
        this.producer   = producer;
    }

    public Expense addExpense(Expense expense){
        Expense saved = expenseRepository.save(expense);
        String userId = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        ActivityEvent evt = new ActivityEvent(
                userId,
                "CREATED",
                "EXPENSE",
                saved.getId(),               // which resource
                Instant.now()                // when
        );
        producer.send(evt);
        return saved;
    }

    public Optional<Expense> getExpenseById(String id){
        return expenseRepository.findById(id);
    }

    public Expense updateExpense(String id, Expense expenseDetails){
        Optional<Expense> optionalExpense = expenseRepository.findById(id);

        if(optionalExpense.isPresent()){
            Expense expense = optionalExpense.get();
            expense.setDescription(expenseDetails.getDescription());
            expense.setAmount(expenseDetails.getAmount());
            expense.setCategory(expenseDetails.getCategory());
            expense.setDate(expenseDetails.getDate());

            Expense updated = expenseRepository.save(expense);
            String userId = SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getName();
            ActivityEvent evt = new ActivityEvent(
                    userId,
                    "UPDATED",
                    "EXPENSE",
                    updated.getId(),               // which resource
                    Instant.now()                // when
            );
            producer.send(evt);
            return updated;
        }

        return null;
    }

    public List<Expense> filterExpenses(String userId, String category, Date startDate, Date endDate) {
        if (category != null && startDate != null && endDate != null) {
            return expenseRepository.findByUserIdAndCategoryAndDateBetween(userId, category, startDate, endDate);
        } else if (category != null && startDate != null) {
            return expenseRepository.findByUserIdAndCategoryAndDateAfter(userId, category, startDate);
        } else if (category != null && endDate != null) {
            return expenseRepository.findByUserIdAndCategoryAndDateBefore(userId, category, endDate);
        } else if (startDate != null && endDate != null) {
            return expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
        } else if (category != null) {
            return expenseRepository.findByUserIdAndCategory(userId, category);
        } else if (startDate != null) {
            return expenseRepository.findByUserIdAndDateAfter(userId, startDate);
        } else if (endDate != null) {
            return expenseRepository.findByUserIdAndDateBefore(userId, endDate);
        } else {
            return expenseRepository.findByUserId(userId);
        }
    }

    public boolean deleteExpense(String id){
        Optional<Expense> expense = expenseRepository.findById(id);
        if(expense.isPresent()){
            expenseRepository.delete(expense.get());
            String userId = SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getName();
            ActivityEvent evt = new ActivityEvent(
                    userId,
                    "DELETED",
                    "EXPENSE",
                    id,               // which resource
                    Instant.now()                // when
            );
            producer.send(evt);
            return true;
        }

        return false;
    }
}
