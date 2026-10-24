package com.stockbridge.inventory.infrastructure.adapter.in.rest;

import com.stockbridge.inventory.domain.model.Role;
import jakarta.validation.constraints.NotNull;

public record ChangeRoleRequest(@NotNull Role role) {}
