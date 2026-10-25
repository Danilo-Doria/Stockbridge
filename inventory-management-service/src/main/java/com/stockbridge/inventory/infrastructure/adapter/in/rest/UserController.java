package com.stockbridge.inventory.infrastructure.adapter.in.rest;

import com.stockbridge.inventory.application.port.in.ChangeUserRoleUseCase;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Usuarios", description = "Administración de usuarios. Requiere rol ADMIN")
public class UserController {
    private final ChangeUserRoleUseCase changeRole;
    public UserController(ChangeUserRoleUseCase changeRole) { this.changeRole = changeRole; }

    @Operation(summary = "Cambiar rol de un usuario",
            description = "Requiere ADMIN. El rol viaja dentro del JWT, así que el usuario debe volver "
                    + "a iniciar sesión para que el cambio se refleje.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rol actualizado"),
            @ApiResponse(responseCode = "400", description = "Rol inválido o id con formato incorrecto"),
            @ApiResponse(responseCode = "403", description = "Sin rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PatchMapping("/{id}/role")
    public UserResponse changeRole(@Parameter(description = "Id del usuario") @PathVariable Long id,
                                   @Valid @RequestBody ChangeRoleRequest r) {
        return UserResponse.from(changeRole.changeRole(id, r.role()));
    }
}
