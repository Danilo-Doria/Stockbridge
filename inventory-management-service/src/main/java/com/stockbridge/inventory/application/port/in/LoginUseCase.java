package com.stockbridge.inventory.application.port.in;

import com.stockbridge.inventory.application.common.AccessToken;

public interface LoginUseCase {
    AccessToken login(LoginCommand command);
    record LoginCommand(String email, String password) {}
}
