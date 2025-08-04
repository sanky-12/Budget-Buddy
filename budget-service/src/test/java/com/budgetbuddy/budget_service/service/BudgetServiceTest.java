package com.budgetbuddy.budget_service.service;

import com.budgetbuddy.budget_service.model.Budget;
import com.budgetbuddy.budget_service.repository.BudgetRepository;
import com.budgetbuddy.budget_service.kafka.ActivityProducer;
import com.budgetbuddy.events.ActivityEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository repo;

    @Mock
    private ActivityProducer producer;

    @InjectMocks
    private BudgetService svc;

    private final String USER = "test-user";

    @BeforeEach
    void setUpSecurityContext() {
        var auth = new TestingAuthenticationToken(USER, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    //-------------- addBudgets --------------

    @Test
    void addBudgets_savesAllAndPublishesBatchEvent() {
        // Arrange: 3 budgets without IDs
        Budget b1 = new Budget(); b1.setCategory("A"); b1.setLimitAmount(10); b1.setMonthYear("2025-07");
        Budget b2 = new Budget(); b2.setCategory("B"); b2.setLimitAmount(20); b2.setMonthYear("2025-07");
        Budget b3 = new Budget(); b3.setCategory("C"); b3.setLimitAmount(30); b3.setMonthYear("2025-07");

        List<Budget> input = List.of(b1, b2, b3);

        // Simulate saved budgets with IDs
        Budget sb1 = new Budget(); sb1.setId("x1"); sb1.setCategory("A"); sb1.setLimitAmount(10); sb1.setMonthYear("2025-07");
        Budget sb2 = new Budget(); sb2.setId("x2"); sb2.setCategory("B"); sb2.setLimitAmount(20); sb2.setMonthYear("2025-07");
        Budget sb3 = new Budget(); sb3.setId("x3"); sb3.setCategory("C"); sb3.setLimitAmount(30); sb3.setMonthYear("2025-07");

        List<Budget> savedList = List.of(sb1, sb2, sb3);
        when(repo.saveAll(input)).thenReturn(savedList);

        // Act
        List<Budget> result = svc.addBudgets(input);

        // Assert return
        assertThat(result).isSameAs(savedList);

        // Verify repository call
        verify(repo).saveAll(input);

        // Capture and verify event
        ArgumentCaptor<ActivityEvent> cap = ArgumentCaptor.forClass(ActivityEvent.class);
        verify(producer).send(cap.capture());
        ActivityEvent evt = cap.getValue();

        assertThat(evt.getUserId()).isEqualTo(USER);
        assertThat(evt.getAction()).isEqualTo("CREATED_BATCH");
        assertThat(evt.getEntityType()).isEqualTo("BUDGET");
        assertThat(evt.getEntityId())
                .contains("3 budgets created")
                .contains("x1,x2,x3");
        assertThat(evt.getTimestamp()).isCloseTo(Instant.now(), within(1L, ChronoUnit.SECONDS));
    }

    //-------------- getBudgetsByUser --------------

    @Test
    void getBudgetsByUser_delegatesToRepository() {
        when(repo.findByUserId(USER)).thenReturn(List.of());
        List<Budget> out = svc.getBudgetsByUser(USER);
        assertThat(out).isEmpty();
        verify(repo).findByUserId(USER);
    }

    //-------------- getBudget --------------

    @Test
    void getBudget_delegatesToRepository() {
        String cat = "Food", mY = "2025-07";
        when(repo.findByUserIdAndCategoryAndMonthYear(USER, cat, mY))
                .thenReturn(Optional.of(new Budget()));
        Optional<Budget> opt = svc.getBudget(USER, cat, mY);
        assertThat(opt).isPresent();
        verify(repo).findByUserIdAndCategoryAndMonthYear(USER, cat, mY);
    }

    //-------------- updateBudget --------------

    @Test
    void updateBudget_savesAndPublishesUpdatedEvent() {
        String id = "b1";
        Budget in = new Budget();
        in.setUserId(USER);
        in.setCategory("X");
        in.setLimitAmount(100);
        in.setMonthYear("2025-08");

        Budget saved = new Budget();
        saved.setId(id);
        saved.setUserId(USER);
        saved.setCategory("X");
        saved.setLimitAmount(100);
        saved.setMonthYear("2025-08");

        when(repo.save(in)).thenReturn(saved);

        Budget out = svc.updateBudget(id, in);

        assertThat(out).isEqualTo(saved);
        verify(repo).save(in);

        ArgumentCaptor<ActivityEvent> cap = ArgumentCaptor.forClass(ActivityEvent.class);
        verify(producer).send(cap.capture());
        ActivityEvent evt = cap.getValue();
        assertThat(evt.getAction()).isEqualTo("UPDATED");
        assertThat(evt.getEntityType()).isEqualTo("BUDGET");
        assertThat(evt.getEntityId()).isEqualTo(id);
    }

    //-------------- getBudgetsByUserAndMonth --------------

    @Test
    void getBudgetsByUserAndMonth_delegatesToRepository() {
        String mY = "2025-07";
        when(repo.findByUserIdAndMonthYear(USER, mY))
                .thenReturn(List.of());
        List<Budget> out = svc.getBudgetsByUserAndMonth(USER, mY);
        assertThat(out).isEmpty();
        verify(repo).findByUserIdAndMonthYear(USER, mY);
    }

    //-------------- copyBudgets --------------

    @Test
    void copyBudgets_existingTarget_throwsException() {
        String from = "2025-06", to = "2025-07";
        when(repo.findByUserIdAndMonthYear(USER, to))
                .thenReturn(List.of(new Budget())); // already exists
        assertThatThrownBy(() -> svc.copyBudgets(USER, from, to))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already exist");
    }

    @Test
    void copyBudgets_nonExisting_createsAndSavesList() {
        String from = "2025-06", to = "2025-07";

        // no budgets at target
        when(repo.findByUserIdAndMonthYear(USER, to)).thenReturn(List.of());
        // mock existing source month
        Budget src = new Budget();
        src.setCategory("A");
        src.setLimitAmount(50);
        src.setMonthYear(from);
        when(repo.findByUserIdAndMonthYear(USER, from)).thenReturn(List.of(src));

        // intercept saveAll
        ArgumentCaptor<List<Budget>> capList = ArgumentCaptor.forClass(List.class);
        when(repo.saveAll(capList.capture())).thenReturn(List.of());

        List<Budget> out = svc.copyBudgets(USER, from, to);

        // Check that the list passed to saveAll has monthYear = to
        List<Budget> toSave = capList.getValue();
        assertThat(toSave).hasSize(1);
        assertThat(toSave.get(0).getMonthYear()).isEqualTo(to);
        assertThat(toSave.get(0).getCategory()).isEqualTo("A");
    }
}
