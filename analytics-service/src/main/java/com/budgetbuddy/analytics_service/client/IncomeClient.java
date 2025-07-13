package com.budgetbuddy.analytics_service.client;

import com.budgetbuddy.analytics_service.model.Income;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Service
public class IncomeClient {

    @Autowired
    private RestTemplate restTemplate;

    public double getTotalIncome(String userId) {
        String url = "http://localhost:8083/income";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getAuthToken());

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Income[]> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, Income[].class);

        return Arrays.stream(response.getBody()).mapToDouble(Income::getAmount).sum();
    }

    private String getAuthToken() {
        return SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
    }
}
