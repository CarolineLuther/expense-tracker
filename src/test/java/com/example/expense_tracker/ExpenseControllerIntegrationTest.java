package com.example.expense_tracker;

import com.example.expense_tracker.model.Category;
import com.example.expense_tracker.model.Expense;
import com.example.expense_tracker.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ExpenseControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ExpenseRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    private String json(String description, String amount, String category, String date) {
        return """
                {"description":"%s","amount":%s,"category":"%s","date":"%s"}
                """.formatted(description, amount, category, date);
    }

    @Test
    void postValidExpenseReturns201AndIsSaved() throws Exception {
        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Lunch", "95.50", "FOOD", "2026-09-10")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.description").value("Lunch"));

        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void postNegativeAmountReturns400() throws Exception {
        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Fel", "-5", "FOOD", "2026-09-10")))
                .andExpect(status().isBadRequest());

        assertThat(repository.count()).isZero();
    }

    @Test
    void getReturnsSavedExpenses() throws Exception {
        repository.save(new Expense("Buss", new BigDecimal("39.00"), Category.TRANSPORT, LocalDate.of(2026, 9, 3)));

        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value("Buss"));
    }

    @Test
    void getWithMonthOnlyReturnsThatMonth() throws Exception {
        repository.save(new Expense("Sept", new BigDecimal("10"), Category.FOOD, LocalDate.of(2026, 9, 30)));
        repository.save(new Expense("Okt", new BigDecimal("10"), Category.FOOD, LocalDate.of(2026, 10, 1)));

        mockMvc.perform(get("/api/expenses").param("month", "2026-09"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value("Sept"));
    }

    @Test
    void deleteRemovesExpense() throws Exception {
        Expense saved = repository.save(new Expense("Bio", new BigDecimal("120"), Category.ENTERTAINMENT, LocalDate.now()));

        mockMvc.perform(delete("/api/expenses/" + saved.getId()))
                .andExpect(status().isNoContent());

        assertThat(repository.count()).isZero();
    }

    @Test
    void deleteUnknownIdReturns404() throws Exception {
        mockMvc.perform(delete("/api/expenses/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void summaryReturnsTotalsPerCategory() throws Exception {
        repository.save(new Expense("A", new BigDecimal("100.00"), Category.FOOD, LocalDate.of(2026, 9, 1)));
        repository.save(new Expense("B", new BigDecimal("50.50"), Category.FOOD, LocalDate.of(2026, 9, 2)));
        repository.save(new Expense("C", new BigDecimal("30.00"), Category.TRANSPORT, LocalDate.of(2026, 9, 3)));

        mockMvc.perform(get("/api/expenses/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perCategory.FOOD").value(150.5))
                .andExpect(jsonPath("$.perCategory.TRANSPORT").value(30.0))
                .andExpect(jsonPath("$.total").value(180.5));
    }
}