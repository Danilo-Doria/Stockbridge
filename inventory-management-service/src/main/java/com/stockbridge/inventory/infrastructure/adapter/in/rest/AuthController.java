package com.stockbridge.inventory.infrastructure.adapter.in.rest;

import com.stockbridge.inventory.application.common.AccessToken;
import com.stockbridge.inventory.application.port.in.LoginUseCase;
import com.stockbridge.inventory.application.port.in.RegisterUserUseCase;
import com.stockbridge.inventory.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "Registro e inicio de sesión. Endpoints públicos")
public class AuthController {

    private final RegisterUserUseCase register;
    private final LoginUseCase login;

    public AuthController(RegisterUserUseCase register, LoginUseCase login) {
        this.register = register; this.login = login;
    }

    @Operation(summary = "Registrar usuario",
            description = "Crea una cuenta con rol EMPLEADO. El rol no se puede elegir al registrarse; "
                    + "un ADMIN puede ascenderlo después.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (errores por campo)"),
            @ApiResponse(responseCode = "409", description = "El email ya está registrado")
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest r) {
        User u = register.register(new RegisterUserUseCase.RegisterCommand(r.fullName(), r.email(), r.password()));
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(u));
    }

    @Operation(summary = "Iniciar sesión",
            description = "Devuelve un JWT (Bearer) para usar en el botón Authorize. "
                    + "El rol viaja dentro del token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticado, token emitido"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest r) {
        AccessToken t = login.login(new LoginUseCase.LoginCommand(r.email(), r.password()));
        return new TokenResponse(t.value(), "Bearer", t.expiresInSeconds());
    }
}
