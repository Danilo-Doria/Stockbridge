package com.stockbridge.inventory.application.port.out;

import com.stockbridge.inventory.domain.model.User;

public interface TokenProviderPort {
    AccessToken issue(User user);
}
