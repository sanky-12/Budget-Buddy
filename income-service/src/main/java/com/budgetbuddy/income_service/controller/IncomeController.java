package com.budgetbuddy.income_service.controller;

import com.budgetbuddy.income_service.model.Income;
import com.budgetbuddy.income_service.service.IncomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/income")
public class IncomeController {

    @Autowired
    private IncomeService incomeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Income addIncome(@RequestBody Income income) {
        income.setUserId(getCurrentUserId());
        return incomeService.addIncome(income);
    }

//    @GetMapping
//    public List<Income> getAllIncome() {
//        return incomeService.getAllIncome(getCurrentUserId());
//    }

    @GetMapping
    public List<Income> getIncome(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) throws ParseException {
        String userId = getCurrentUserId();
        // both dates provided → filter by range
        if (startDate != null && endDate != null) {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
            Date start = fmt.parse(startDate);
            Date end   = fmt.parse(endDate);
            return incomeService.getIncomeByDateRange(userId, start, end);
        }
        // otherwise return all
        return incomeService.getAllIncome(userId);
    }

    @GetMapping("/{id}")
    public Optional<Income> getIncomeById(@PathVariable String id) {
        return incomeService.getIncomeById(id);
    }

    @PutMapping("/{id}")
    public Income updateIncome(@PathVariable String id, @RequestBody Income income) {
        income.setUserId(getCurrentUserId());
        return incomeService.updateIncome(id, income);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteIncome(@PathVariable String id) {
        incomeService.deleteIncome(id);
    }

//    @GetMapping("/daterange")
//    public List<Income> getIncomeByDateRange(@RequestParam String startDate,
//                                             @RequestParam String endDate) throws Exception {
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//        Date start = format.parse(startDate);
//        Date end = format.parse(endDate);
//        return incomeService.getIncomeByDateRange(getCurrentUserId(), start, end);
//    }

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}