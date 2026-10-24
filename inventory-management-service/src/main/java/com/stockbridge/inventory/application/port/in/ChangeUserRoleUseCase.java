package com.stockbridge.inventory.application.port.in;

import com.stockbridge.inventory.domain.model.Role;
import com.stockbridge.inventory.domain.model.User;

public interface ChangeUserRoleUseCase {
    User changeRole(Long userId, Role role);
}
