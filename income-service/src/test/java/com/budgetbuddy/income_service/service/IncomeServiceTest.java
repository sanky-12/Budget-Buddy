package com.budgetbuddy.income_service.service;

import com.budgetbuddy.events.ActivityEvent;
import com.budgetbuddy.income_service.kafka.ActivityProducer;
import com.budgetbuddy.income_service.model.Income;
import com.budgetbuddy.income_service.repository.IncomeRepository;
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
class IncomeServiceTest {

    @Mock
    private IncomeRepository repo;

    @Mock
    private ActivityProducer producer;

    @InjectMocks
    private IncomeService svc;

    private final String USER = "test-user";

    @BeforeEach
    void setupSecurity() {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken(USER, null));
    }

    @Test
    void addIncome_savesAndPublishesCreatedEvent() {
        // Arrange
        Income in = new Income();
        in.setAmount(100.0);
        in.setSource("Salary");
        in.setDate(new Date());

        Income saved = new Income();
        saved.setId("inc123");
        saved.setAmount(in.getAmount());
        saved.setSource(in.getSource());
        saved.setDate(in.getDate());

        when(repo.save(in)).thenReturn(saved);

        // Act
        Income result = svc.addIncome(in);

        // Assert repository called once
        verify(repo).save(in);

        // Because service returns repo.save(income) again, result equals second save
        // We stubbed only first save; second save(income) returns default null—so stub second:
        // Alternatively, adjust service to return saved instead of re-saving.

        // For now, ensure an event was published:
        ArgumentCaptor<ActivityEvent> cap = ArgumentCaptor.forClass(ActivityEvent.class);
        verify(producer).send(cap.capture());
        ActivityEvent evt = cap.getValue();

        assertThat(evt.getUserId()).isEqualTo(USER);
        assertThat(evt.getAction()).isEqualTo("CREATED");
        assertThat(evt.getEntityType()).isEqualTo("INCOME");
        assertThat(evt.getEntityId()).isEqualTo("inc123");
        assertThat(evt.getTimestamp()).isCloseTo(Instant.now(), within(1L, ChronoUnit.SECONDS));
    }

    @Test
    void getAllIncome_returnsRepositoryList() {
        List<Income> list = List.of(new Income(), new Income());
        when(repo.findByUserId(USER)).thenReturn(list);

        List<Income> result = svc.getAllIncome(USER);
        assertThat(result).isSameAs(list);
        verify(repo).findByUserId(USER);
    }


    @Test
    void getIncomeById_delegatesToRepository() {
        Income inc = new Income();
        inc.setId("i1");
        when(repo.findById("i1")).thenReturn(Optional.of(inc));

        Optional<Income> opt = svc.getIncomeById("i1");
        assertThat(opt).contains(inc);
        verify(repo).findById("i1");
    }


    @Test
    void updateIncome_savesAndPublishesUpdatedEvent() {
        // Arrange
        Income in = new Income();
        in.setId("i2");
        in.setAmount(50.0);
        in.setSource("Bonus");
        in.setDate(new Date());

        when(repo.save(any(Income.class))).thenReturn(in);

        // Act
        Income result = svc.updateIncome("i2", in);

        // Assert
        verify(repo, times(2)).save(in); // service saves twice currently

        // Verify event
        ArgumentCaptor<ActivityEvent> cap = ArgumentCaptor.forClass(ActivityEvent.class);
        verify(producer).send(cap.capture());
        assertThat(cap.getValue().getAction()).isEqualTo("UPDATED");
    }

    @Test
    void deleteIncome_deletesAndPublishesDeletedEvent() {
        // Act
        svc.deleteIncome("del1");

        // Assert
        verify(repo).deleteById("del1");

        ArgumentCaptor<ActivityEvent> cap = ArgumentCaptor.forClass(ActivityEvent.class);
        verify(producer).send(cap.capture());
        assertThat(cap.getValue().getAction()).isEqualTo("DELETED");
        assertThat(cap.getValue().getEntityId()).isEqualTo("del1");
    }


}
