package com.example.expense_tracker.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(name = "expense_date")
    private LocalDate date;

    protected Expense() {} // krävs av JPA

    public Expense(String description, BigDecimal amount, Category category, LocalDate date) {
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.date = date;
    }

    public Long getId() { return id; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }
    public Category getCategory() { return category; }
    public LocalDate getDate() { return date; }

    public void setDescription(String description) { this.description = description; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setCategory(Category category) { this.category = category; }
    public void setDate(LocalDate date) { this.date = date; }
}