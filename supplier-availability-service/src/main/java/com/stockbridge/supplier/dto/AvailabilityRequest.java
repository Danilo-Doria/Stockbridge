package com.stockbridge.supplier.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record AvailabilityRequest(@NotBlank String productCode, @Positive int quantity, @NotBlank String category) {}