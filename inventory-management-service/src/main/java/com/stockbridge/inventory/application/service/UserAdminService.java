package com.stockbridge.inventory.application.service;

import com.stockbridge.inventory.application.port.in.ChangeUserRoleUseCase;
import com.stockbridge.inventory.application.port.out.UserRepositoryPort;
import com.stockbridge.inventory.domain.exception.UserNotFoundException;
import com.stockbridge.inventory.domain.model.Role;
import com.stockbridge.inventory.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserAdminService implements ChangeUserRoleUseCase {
    private final UserRepositoryPort users;
    public UserAdminService(UserRepositoryPort users) { this.users = users; }

    @Override
    public User changeRole(Long userId, Role role) {
        User user = users.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        user.changeRole(role);
        return users.save(user);
    }
}
