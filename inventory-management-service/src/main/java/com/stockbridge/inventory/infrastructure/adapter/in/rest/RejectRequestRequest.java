package com.stockbridge.inventory.infrastructure.adapter.in.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectRequestRequest(@NotBlank @Size(max = 500) String reason) {}

