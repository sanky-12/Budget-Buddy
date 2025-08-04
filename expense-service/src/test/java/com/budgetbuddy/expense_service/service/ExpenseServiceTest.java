package com.budgetbuddy.expense_service.service;


import com.budgetbuddy.expense_service.model.Expense;
import com.budgetbuddy.expense_service.repository.ExpenseRepository;
import com.budgetbuddy.expense_service.kafka.ActivityProducer;
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
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {
    @Mock
    private ExpenseRepository repo;

    @Mock
    private ActivityProducer producer;

    @InjectMocks
    private ExpenseService svc;

    // a fixed userId for all tests
    private final String USER = "test-user";

    @BeforeEach
    void setUpSecurityContext() {
        var auth = new TestingAuthenticationToken(USER, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void addExpense_savesAndPublishesCreatedEvent() {
        // arrange
        Expense in = new Expense();
        in.setDescription("Coffee");
        in.setAmount(3.5);
        in.setCategory("Food");
        in.setDate(new Date());

        Expense saved = new Expense();
        saved.setId("exp123");
        saved.setDescription(in.getDescription());
        saved.setAmount(in.getAmount());
        saved.setCategory(in.getCategory());
        saved.setDate(in.getDate());

        when(repo.save(in)).thenReturn(saved);

        // act
        Expense result = svc.addExpense(in);

        // assert returned value
        assertThat(result).isEqualTo(saved);

        // verify repo.save called
        verify(repo).save(in);

        // capture the event published
        ArgumentCaptor<ActivityEvent> eventCaptor = ArgumentCaptor.forClass(ActivityEvent.class);
        verify(producer).send(eventCaptor.capture());

        ActivityEvent evt = eventCaptor.getValue();
        assertThat(evt.getUserId()).isEqualTo(USER);
        assertThat(evt.getAction()).isEqualTo("CREATED");
        assertThat(evt.getEntityType()).isEqualTo("EXPENSE");
        assertThat(evt.getEntityId()).isEqualTo("exp123");
        assertThat(evt.getTimestamp()).isCloseTo(Instant.now(), within(1L, ChronoUnit.SECONDS));
    }

    @Test
    void updateExpense_whenExists_updatesAndPublishes() {
        String id = "e1";
        Expense original = new Expense();
        original.setId(id);
        original.setDescription("Old");
        original.setAmount(10.0);
        original.setCategory("Misc");
        original.setDate(new Date());

        Expense details = new Expense();
        details.setDescription("New");
        details.setAmount(20.0);
        details.setCategory("Office");
        details.setDate(new Date());

        when(repo.findById(id)).thenReturn(Optional.of(original));
        when(repo.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));

        Expense updated = svc.updateExpense(id, details);

        assertThat(updated.getDescription()).isEqualTo("New");
        assertThat(updated.getAmount()).isEqualTo(20.0);
        assertThat(updated.getCategory()).isEqualTo("Office");

        verify(repo).save(original);

        // event
        ArgumentCaptor<ActivityEvent> cap = ArgumentCaptor.forClass(ActivityEvent.class);
        verify(producer).send(cap.capture());
        assertThat(cap.getValue().getAction()).isEqualTo("UPDATED");
    }

    @Test
    void updateExpense_whenMissing_returnsNullAndNoEvent() {
        when(repo.findById("does-not-exist")).thenReturn(Optional.empty());

        Expense result = svc.updateExpense("does-not-exist", new Expense());
        assertThat(result).isNull();

        verify(repo, never()).save(any());
        verify(producer, never()).send(any());
    }


    @Test
    void filterExpenses_noFilters_callsFindByUserId() {
        when(repo.findByUserId(USER)).thenReturn(List.of());
        List<Expense> list = svc.filterExpenses(USER, null, null, null);
        assertThat(list).isEmpty();
        verify(repo).findByUserId(USER);
    }


    @Test
    void filterExpenses_categoryOnly_callsCategoryMethod() {
        String cat = "Food";
        when(repo.findByUserIdAndCategory(USER, cat)).thenReturn(List.of());
        svc.filterExpenses(USER, cat, null, null);
        verify(repo).findByUserIdAndCategory(USER, cat);
    }

    @Test
    void filterExpenses_dateRangeOnly_callsDateBetween() {
        Date start = new Date(1000), end = new Date(2000);
        when(repo.findByUserIdAndDateBetween(USER, start, end))
                .thenReturn(List.of());
        svc.filterExpenses(USER, null, start, end);
        verify(repo).findByUserIdAndDateBetween(USER, start, end);
    }

    @Test
    void filterExpenses_allFilters_callsCategoryAndDateBetween() {
        Date start = new Date(1000), end = new Date(2000);
        String cat = "Rent";
        when(repo.findByUserIdAndCategoryAndDateBetween(USER, cat, start, end))
                .thenReturn(List.of());
        svc.filterExpenses(USER, cat, start, end);
        verify(repo).findByUserIdAndCategoryAndDateBetween(USER, cat, start, end);
    }

    @Test
    void deleteExpense_whenExists_deletesAndPublishes() {
        String id = "d1";
        Expense e = new Expense(); e.setId(id);
        when(repo.findById(id)).thenReturn(Optional.of(e));

        boolean result = svc.deleteExpense(id);
        assertThat(result).isTrue();

        verify(repo).delete(e);
        ArgumentCaptor<ActivityEvent> cap = ArgumentCaptor.forClass(ActivityEvent.class);
        verify(producer).send(cap.capture());
        assertThat(cap.getValue().getAction()).isEqualTo("DELETED");
    }

    @Test
    void deleteExpense_whenMissing_returnsFalse() {
        when(repo.findById("nope")).thenReturn(Optional.empty());
        assertThat(svc.deleteExpense("nope")).isFalse();
        verify(repo, never()).delete(any());
        verify(producer, never()).send(any());
    }


}
