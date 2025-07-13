package com.budgetbuddy.analytics_service.client;


import com.budgetbuddy.analytics_service.model.Expense;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;


@Service
public class ExpenseClient {

    @Autowired
    private RestTemplate restTemplate;

    public List<Expense> getExpenses(String userId) {
        String url = "http://localhost:8082/expenses";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getAuthToken());

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Expense[]> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, Expense[].class);

        return Arrays.asList(response.getBody());
    }

    private String getAuthToken() {
        return SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
    }
}
