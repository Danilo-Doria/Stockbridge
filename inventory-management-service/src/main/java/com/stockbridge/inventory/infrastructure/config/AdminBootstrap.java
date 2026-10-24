package com.stockbridge.inventory.infrastructure.config;

import com.stockbridge.inventory.application.port.out.PasswordHasherPort;
import com.stockbridge.inventory.application.port.out.UserRepositoryPort;
import com.stockbridge.inventory.domain.model.Role;
import com.stockbridge.inventory.domain.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements CommandLineRunner {

    private final UserRepositoryPort users;
    private final PasswordHasherPort hasher;
    private final String name, email, password;

    public AdminBootstrap(UserRepositoryPort users, PasswordHasherPort hasher,
                          @Value("${app.security.bootstrap-admin.name}") String name,
                          @Value("${app.security.bootstrap-admin.email}") String email,
                          @Value("${app.security.bootstrap-admin.password}") String password) {
        this.users = users; this.hasher = hasher;
        this.name = name; this.email = email; this.password = password;
    }

    @Override
    public void run(String... args) {
        if (!users.existsByEmail(email.toLowerCase())) {
            users.save(User.create(name, email, hasher.hash(password), Role.ADMIN));
        }
    }
}