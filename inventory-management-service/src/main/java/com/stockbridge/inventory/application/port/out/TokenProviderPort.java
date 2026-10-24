package com.stockbridge.inventory.application.port.out;

import com.stockbridge.inventory.application.common.AccessToken;
import com.stockbridge.inventory.domain.model.User;

public interface TokenProviderPort {
    AccessToken issue(User user);
}
