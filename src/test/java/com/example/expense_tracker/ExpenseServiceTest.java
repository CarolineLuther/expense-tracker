package com.example.expense_tracker;

import com.example.expense_tracker.model.Category;
import com.example.expense_tracker.model.Expense;
import com.example.expense_tracker.model.ExpenseSummary;
import com.example.expense_tracker.repository.ExpenseRepository;
import com.example.expense_tracker.service.ExpenseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    ExpenseRepository repository;

    @InjectMocks
    ExpenseService service;

    private Expense expense(Category category, String amount) {
        return new Expense("Test", new BigDecimal(amount), category, LocalDate.of(2026, 9, 10));
    }

    @Test
    void summaryGroupsAmountsPerCategory() {
        when(repository.findAll()).thenReturn(List.of(
                expense(Category.FOOD, "100.00"),
                expense(Category.FOOD, "50.50"),
                expense(Category.TRANSPORT, "30.00")));

        ExpenseSummary summary = service.getSummary(null);

        assertThat(summary.perCategory().get(Category.FOOD)).isEqualByComparingTo("150.50");
        assertThat(summary.perCategory().get(Category.TRANSPORT)).isEqualByComparingTo("30.00");
        assertThat(summary.total()).isEqualByComparingTo("180.50");
    }

    @Test
    void summaryOfNoExpensesIsZero() {
        when(repository.findAll()).thenReturn(List.of());

        ExpenseSummary summary = service.getSummary(null);

        assertThat(summary.perCategory()).isEmpty();
        assertThat(summary.total()).isEqualByComparingTo("0");
    }

    @Test
    void monthFilterUsesFirstAndLastDayOfMonth() {
        service.getAll(YearMonth.of(2026, 2));

        verify(repository).findByDateBetween(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-5"})
    void createRejectsZeroOrNegativeAmount(String amount) {
        assertThatThrownBy(() -> service.create(expense(Category.FOOD, amount)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void createRejectsBlankDescription() {
        Expense e = new Expense("  ", new BigDecimal("10"), Category.FOOD, LocalDate.now());

        assertThatThrownBy(() -> service.create(e))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createSavesValidExpense() {
        Expense e = expense(Category.FOOD, "25.00");
        when(repository.save(e)).thenReturn(e);

        assertThat(service.create(e)).isSameAs(e);
    }

    @Test
    void deleteUnknownIdThrowsNotFound() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(NoSuchElementException.class);
    }
}
