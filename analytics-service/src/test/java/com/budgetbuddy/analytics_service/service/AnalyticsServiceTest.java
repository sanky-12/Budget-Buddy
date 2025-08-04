package com.budgetbuddy.analytics_service.service;

import com.budgetbuddy.analytics_service.client.BudgetClient;
import com.budgetbuddy.analytics_service.client.ExpenseClient;
import com.budgetbuddy.analytics_service.client.IncomeClient;
import com.budgetbuddy.analytics_service.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    ExpenseClient expenseClient;

    @Mock
    BudgetClient budgetClient;

    @Mock
    IncomeClient incomeClient;

    @InjectMocks
    AnalyticsService analyticsService;

    private final String USER = "user1";

    private Expense e1,e2;
    private Budget b1,b2;

    @BeforeEach
    void setUp() {
        // Sample expenses
        e1 = new Expense();
        e1.setCategory("Food");
        e1.setAmount(50.0);
        e1.setDate(Date.from(java.time.Instant.parse("2025-07-01T00:00:00Z")));
        e1.setId("exp1");

        e2 = new Expense();
        e2.setCategory("Travel");
        e2.setAmount(20.0);
        e2.setDate(Date.from(java.time.Instant.parse("2025-07-05T00:00:00Z")));
        e2.setId("exp2");

        // Sample budgets
        b1 = new Budget();
        b1.setCategory("Food");
        b1.setLimitAmount(100.0);
        b1.setMonthYear("2025-07");
        b1.setId("bud1");

        b2 = new Budget();
        b2.setCategory("Travel");
        b2.setLimitAmount(30.0);
        b2.setMonthYear("2025-07");
        b2.setId("bud2");
    }

    @Test
    void getUserAnalytics_noFilter_sumsAll() {
        // Arrange: expenseClient returns both, budgetClient returns both
        when(expenseClient.getExpenses(USER)).thenReturn(List.of(e1, e2));
        when(budgetClient.getBudgets(USER)).thenReturn(List.of(b1, b2));
        when(incomeClient.getTotalIncome(USER)).thenReturn(200.0);

        // Act
        AnalyticsResponse resp = analyticsService.getUserAnalytics(USER, null);

        // Assert totals
        assertThat(resp.getTotalExpenses()).isEqualTo(70.0);
        assertThat(resp.getTotalIncome()).isEqualTo(200.0);
        assertThat(resp.getNetSavings()).isEqualTo(130.0);

        // Budget usage list: two entries
        List<BudgetUsage> usage = resp.getBudgetUsage();
        assertThat(usage).hasSize(2);

        // Check each category
        Map<String, BudgetUsage> byCat = new HashMap<>();
        usage.forEach(u -> byCat.put(u.getCategory(), u));

        BudgetUsage food = byCat.get("Food");
        assertThat(food.getLimit()).isEqualTo(100.0);
        assertThat(food.getSpent()).isEqualTo(50.0);
        assertThat(food.getRemaining()).isEqualTo(50.0);
        assertThat(food.getPercentUsed()).isEqualTo(50.0);

        BudgetUsage travel = byCat.get("Travel");
        assertThat(travel.getLimit()).isEqualTo(30.0);
        assertThat(travel.getSpent()).isEqualTo(20.0);
        assertThat(travel.getRemaining()).isEqualTo(10.0);
        assertThat(travel.getPercentUsed()).isEqualTo((20.0/30.0)*100.0);
    }


    @Test
    void getUserAnalytics_withFilter_filtersByMonthYear() {
        // Add an expense/budget for a different month to test filtering
        Expense eOther = new Expense();
        eOther.setCategory("Food");
        eOther.setAmount(999.0);
        eOther.setDate(Date.from(java.time.Instant.parse("2025-06-01T00:00:00Z")));

        Budget bOther = new Budget();
        bOther.setCategory("Food");
        bOther.setLimitAmount(500);
        bOther.setMonthYear("2025-06");

        when(expenseClient.getExpenses(USER)).thenReturn(List.of(e1, e2, eOther));
        when(budgetClient.getBudgets(USER)).thenReturn(List.of(b1, b2, bOther));
        when(incomeClient.getTotalIncome(USER)).thenReturn(200.0);

        AnalyticsResponse resp = analyticsService.getUserAnalytics(USER, "2025-07");

        // eOther and bOther should be filtered out
        assertThat(resp.getTotalExpenses()).isEqualTo(70.0);
        assertThat(resp.getBudgetUsage()).allMatch(u -> u.getCategory().equals("Food") || u.getCategory().equals("Travel"));
    }

    @Test
    void getAvailableMonths_returnsDistinctSorted() {
        Budget m1 = new Budget(); m1.setMonthYear("2025-06");
        Budget m2 = new Budget(); m2.setMonthYear("2025-07");
        Budget m3 = new Budget(); m3.setMonthYear("2025-07"); // duplicate
        Budget m4 = new Budget(); m4.setMonthYear("2024-12");

        when(budgetClient.getBudgets(USER)).thenReturn(List.of(m1, m2, m3, m4));

        List<String> months = analyticsService.getAvailableMonths(USER);

        // Expect distinct sorted descending: ["2025-07", "2025-06", "2024-12"]
        assertThat(months).containsExactly("2025-07", "2025-06", "2024-12");
    }


    
}
