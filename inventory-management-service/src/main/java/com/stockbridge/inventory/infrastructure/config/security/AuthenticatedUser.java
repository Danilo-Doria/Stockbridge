package com.stockbridge.inventory.infrastructure.config.security;

import com.stockbridge.inventory.domain.model.Role;

public record AuthenticatedUser(Long id, String email, Role role) {}
