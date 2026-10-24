package com.stockbridge.inventory.infrastructure.adapter.in.rest;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 150) String name,
        @Size(max = 500) String description,
        @NotBlank @Size(max = 80) String category,
        @NotNull @Positive BigDecimal price,
        @PositiveOrZero int stock) {}
