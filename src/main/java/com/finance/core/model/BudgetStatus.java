package com.finance.core.model;

import java.math.BigDecimal;

public record BudgetStatus(String category, BigDecimal limit, BigDecimal spent, BigDecimal remaining) { }
