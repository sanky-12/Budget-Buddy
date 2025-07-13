package com.budgetbuddy.income_service.repository;

import com.budgetbuddy.income_service.model.Income;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

public interface IncomeRepository extends MongoRepository<Income, String> {
    List<Income> findByUserId(String userId);
    List<Income> findByUserIdAndDateBetween(String userId, Date start, Date end);
}
