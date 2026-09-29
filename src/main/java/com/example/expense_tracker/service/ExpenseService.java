package com.example.expense_tracker.service;

import com.example.expense_tracker.model.Category;
import com.example.expense_tracker.model.Expense;
import com.example.expense_tracker.model.ExpenseSummary;
import com.example.expense_tracker.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class ExpenseService {

    private final ExpenseRepository repository;

    public ExpenseService(ExpenseRepository repository) {
        this.repository = repository;
    }

    public List<Expense> getAll(YearMonth month) {
        if (month == null) {
            return repository.findAll();
        }
        return repository.findByDateBetween(month.atDay(1), month.atEndOfMonth());
    }

    public Expense create(Expense expense) {
        if (expense.getDescription() == null || expense.getDescription().isBlank()) {
            throw new IllegalArgumentException("Description must not be empty");
        }
        if (expense.getAmount() == null || expense.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        if (expense.getCategory() == null) {
            throw new IllegalArgumentException("Category is required");
        }
        if (expense.getDate() == null) {
            throw new IllegalArgumentException("Date is required");
        }
        return repository.save(expense);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("Expense " + id + " not found");
        }
        repository.deleteById(id);
    }

    public ExpenseSummary getSummary(YearMonth month) {
        Map<Category, BigDecimal> perCategory = new EnumMap<>(Category.class);
        BigDecimal total = BigDecimal.ZERO;
        for (Expense e : getAll(month)) {
            perCategory.merge(e.getCategory(), e.getAmount(), BigDecimal::add);
            total = total.add(e.getAmount());
        }
        return new ExpenseSummary(perCategory, total);
    }
}