package com.stockbridge.inventory.infrastructure.adapter.in.rest;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateRequestRequest(@NotNull Long productId, @Positive int quantity,
                                   @Size(max = 500) String observation) {}
