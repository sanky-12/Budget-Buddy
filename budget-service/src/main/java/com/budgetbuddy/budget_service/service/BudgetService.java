package com.budgetbuddy.budget_service.service;

import com.budgetbuddy.budget_service.kafka.ActivityProducer;
import com.budgetbuddy.budget_service.model.Budget;
import com.budgetbuddy.budget_service.repository.BudgetRepository;
import com.budgetbuddy.events.ActivityEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ActivityProducer producer;
    private static final String TOPIC = "user-activity-logs";

    public BudgetService(BudgetRepository budgetRepository, ActivityProducer producer) {
        this.budgetRepository = budgetRepository;
        this.producer = producer;
    }


    public List<Budget> addBudgets(List<Budget> budgets) {
        List<Budget> savedList = budgetRepository.saveAll(budgets);

        // send one batch event
        String userId = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        // You can encode details however you like, e.g. number + IDs:
        String details = String.format(
                "%d budgets created. IDs: %s",
                savedList.size(),
                savedList.stream()
                        .map(Budget::getId)
                        .collect(Collectors.joining(",")));

        ActivityEvent evt = new ActivityEvent(
                userId,
                "CREATED_BATCH",
                "BUDGET",       // entity type
                details,
                Instant.now()
        );
        producer.send(evt);

        return savedList;
    }

    public List<Budget> getBudgetsByUser(String userId) {
        return budgetRepository.findByUserId(userId);
    }

    public Optional<Budget> getBudget(String userId, String category, String monthYear) {
        return budgetRepository.findByUserIdAndCategoryAndMonthYear(userId, category, monthYear);
    }

    public Budget updateBudget(String id, Budget updatedBudget) {
        updatedBudget.setId(id);
        Budget saved = budgetRepository.save(updatedBudget);
        String userId = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        ActivityEvent evt = new ActivityEvent(
                userId,
                "UPDATED",
                "BUDGET",     // entity type for budget-service
                saved.getId(),
                Instant.now()
        );
        producer.send(evt);
        return saved;
    }


    public List<Budget> getBudgetsByUserAndMonth(String userId, String monthYear) {
        return budgetRepository.findByUserIdAndMonthYear(userId, monthYear);
    }

    public List<Budget> copyBudgets(String userId, String fromMonthYear, String toMonthYear) {
        // Check if budgets already exist for the target month
        List<Budget> existing = budgetRepository.findByUserIdAndMonthYear(userId, toMonthYear);
        if (!existing.isEmpty()) {
            throw new RuntimeException("Budgets for the target month already exist.");
        }

        // Get budgets for the source month
        List<Budget> previous = budgetRepository.findByUserIdAndMonthYear(userId, fromMonthYear);
        List<Budget> copied = previous.stream().map(b -> {
            Budget copy = new Budget();
            copy.setUserId(userId);
            copy.setCategory(b.getCategory());
            copy.setLimitAmount(b.getLimitAmount());
            copy.setMonthYear(toMonthYear);
            return copy;
        }).toList();

        return budgetRepository.saveAll(copied);
    }
}