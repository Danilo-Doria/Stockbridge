package com.stockbridge.inventory.application.port.in;

import com.stockbridge.inventory.domain.model.User;

public interface RegisterUserUseCase {
    User register(RegisterCommand command);
    record RegisterCommand(String fullName, String email, String password) {}
}
