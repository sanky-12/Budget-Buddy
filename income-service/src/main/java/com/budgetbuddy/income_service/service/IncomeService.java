package com.budgetbuddy.income_service.service;


import com.budgetbuddy.events.ActivityEvent;
import com.budgetbuddy.income_service.kafka.ActivityProducer;
import com.budgetbuddy.income_service.model.Income;
import com.budgetbuddy.income_service.repository.IncomeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class IncomeService {


    private final IncomeRepository incomeRepository;
    private final ActivityProducer producer;
    private static final String TOPIC = "user-activity-logs";

    public IncomeService(IncomeRepository incomeRepository, ActivityProducer producer) {
        this.incomeRepository = incomeRepository;
        this.producer = producer;
    }

    public Income addIncome(Income income) {
        Income saved = incomeRepository.save(income);

        String userId = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        ActivityEvent evt = new ActivityEvent(
                userId,
                "CREATED",
                "INCOME",
                saved.getId(),               // which resource
                Instant.now()                // when
        );
        producer.send(evt);
        return incomeRepository.save(income);
    }

    public List<Income> getAllIncome(String userId) {
        return incomeRepository.findByUserId(userId);
    }

    public Optional<Income> getIncomeById(String id) {
        return incomeRepository.findById(id);
    }

    public Income updateIncome(String id, Income updatedIncome) {
        updatedIncome.setId(id);

        Income saved = incomeRepository.save(updatedIncome);

        String userId = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        ActivityEvent evt = new ActivityEvent(
                userId,
                "UPDATED",
                "INCOME",
                saved.getId(),               // which resource
                Instant.now()                // when
        );
        producer.send(evt);
        return incomeRepository.save(updatedIncome);
    }

    public void deleteIncome(String id) {
        incomeRepository.deleteById(id);

        String userId = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        ActivityEvent evt = new ActivityEvent(
                userId,
                "DELETED",
                "INCOME",
                id,               // which resource
                Instant.now()                // when
        );
        producer.send(evt);
    }

    public List<Income> getIncomeByDateRange(String userId, Date start, Date end) {
        return incomeRepository.findByUserIdAndDateBetween(userId, start, end);
    }
}