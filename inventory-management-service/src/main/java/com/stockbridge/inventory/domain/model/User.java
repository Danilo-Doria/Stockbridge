package com.stockbridge.inventory.domain.model;

import com.stockbridge.inventory.domain.exception.InvalidUserException;

public class User {

    private final Long id;
    private final String fullName;
    private final String email;
    private final String passwordHash;
    private Role role;
    private boolean active;

    private User(Long id, String fullName, String email, String passwordHash, Role role, boolean active) {
        this.id = id;
        this.fullName = requireText(fullName, "El nombre es obligatorio");
        this.email = requireText(email, "El email es obligatorio");
        this.passwordHash = requireText(passwordHash, "La contraseña es obligatoria");
        if (role == null) throw new InvalidUserException("El rol es obligatorio");
        this.role = role;
        this.active = active;
    }

    public static User create(String fullName, String email, String passwordHash, Role role) {
        String normalized = requireText(email, "El email es obligatorio").toLowerCase();
        return new User(null, fullName, normalized, passwordHash, role, true);
    }

    public static User restore(Long id, String fullName, String email, String passwordHash,
                               Role role, boolean active) {
        if (id == null) throw new InvalidUserException("El id es obligatorio al reconstruir");
        return new User(id, fullName, email, passwordHash, role, active);
    }

    private static String requireText(String v, String msg) {
        if (v == null || v.isBlank()) throw new InvalidUserException(msg);
        return v.trim();
    }

    public void changeRole(Role newRole) {
        if (newRole == null) throw new InvalidUserException("El rol es obligatorio");
        this.role = newRole;
    }

    public boolean isActive() {
        return active;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public Long getId() {
        return id;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }
}
