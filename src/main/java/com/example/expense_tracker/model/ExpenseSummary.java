package com.example.expense_tracker.model;

import java.math.BigDecimal;
import java.util.Map;

public record ExpenseSummary(Map<Category, BigDecimal> perCategory, BigDecimal total) {}
