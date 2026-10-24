package com.stockbridge.inventory.infrastructure.adapter.in.rest;

import com.stockbridge.inventory.domain.model.Role;
import com.stockbridge.inventory.domain.model.User;

public record UserResponse(Long id, String fullName, String email, Role role) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getRole());
    }
}
