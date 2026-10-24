package com.stockbridge.inventory.infrastructure.adapter.in.rest;

import com.stockbridge.inventory.application.port.in.ChangeUserRoleUseCase;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final ChangeUserRoleUseCase changeRole;
    public UserController(ChangeUserRoleUseCase changeRole) { this.changeRole = changeRole; }

    @PatchMapping("/{id}/role")
    public UserResponse changeRole(@PathVariable Long id, @Valid @RequestBody ChangeRoleRequest r) {
        return UserResponse.from(changeRole.changeRole(id, r.role()));
    }
}
