package com.stockbridge.inventory.infrastructure.adapter.in.rest;

import com.stockbridge.inventory.application.common.AccessToken;
import com.stockbridge.inventory.application.port.in.LoginUseCase;
import com.stockbridge.inventory.application.port.in.RegisterUserUseCase;
import com.stockbridge.inventory.domain.model.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RegisterUserUseCase register;
    private final LoginUseCase login;

    public AuthController(RegisterUserUseCase register, LoginUseCase login) {
        this.register = register; this.login = login;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest r) {
        User u = register.register(new RegisterUserUseCase.RegisterCommand(r.fullName(), r.email(), r.password()));
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(u));
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest r) {
        AccessToken t = login.login(new LoginUseCase.LoginCommand(r.email(), r.password()));
        return new TokenResponse(t.value(), "Bearer", t.expiresInSeconds());
    }
}
