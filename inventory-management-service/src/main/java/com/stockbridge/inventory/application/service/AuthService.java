package com.stockbridge.inventory.application.service;

import com.stockbridge.inventory.application.common.AccessToken;
import com.stockbridge.inventory.application.port.in.LoginUseCase;
import com.stockbridge.inventory.application.port.in.RegisterUserUseCase;
import com.stockbridge.inventory.application.port.out.PasswordHasherPort;
import com.stockbridge.inventory.application.port.out.TokenProviderPort;
import com.stockbridge.inventory.application.port.out.UserRepositoryPort;
import com.stockbridge.inventory.domain.exception.EmailAlreadyRegisteredException;
import com.stockbridge.inventory.domain.exception.InvalidCredentialsException;
import com.stockbridge.inventory.domain.model.Role;
import com.stockbridge.inventory.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService implements RegisterUserUseCase, LoginUseCase {

    private final UserRepositoryPort users;
    private final PasswordHasherPort hasher;
    private final TokenProviderPort tokens;

    public AuthService(UserRepositoryPort users, PasswordHasherPort hasher, TokenProviderPort tokens) {
        this.users = users; this.hasher = hasher; this.tokens = tokens;
    }

    @Override
    public User register(RegisterCommand c) {
        String email = c.email().trim().toLowerCase();
        if (users.existsByEmail(email)) throw new EmailAlreadyRegisteredException(email);
        // el registro público SIEMPRE crea EMPLEADO: nadie se autoasigna ADMIN
        User user = User.create(c.fullName(), email, hasher.hash(c.password()), Role.EMPLEADO);
        return users.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AccessToken login(LoginCommand c) {
        User user = users.findByEmail(c.email().trim().toLowerCase())
                .orElseThrow(InvalidCredentialsException::new);
        if (!user.isActive() || !hasher.matches(c.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return tokens.issue(user);
    }
}