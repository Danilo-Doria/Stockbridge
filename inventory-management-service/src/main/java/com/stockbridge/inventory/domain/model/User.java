package com.stockbridge.inventory.domain.model;

import com.stockbridge.inventory.domain.exception.InvalidUserException;

public class User {

    private final Long id;
    private final String fullName;
    private final String email;
    private final String passwordHash;
    private final Role role;
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
        return new User(null, fullName, email.trim().toLowerCase(), passwordHash, role, true);
    }

    public static User restore(Long id, String fullName, String email, String passwordHash,
                               Role role, boolean active) {
        if (id == null) throw new InvalidUserException("El id es obligatorio al reconstruir");
        return new User(id, fullName, email, passwordHash, role, active);
    }

    public void deactivate() { this.active = false; }
    public void activate()   { this.active = true; }

    private static String requireText(String v, String msg) {
        if (v == null || v.isBlank()) throw new InvalidUserException(msg);
        return v.trim();
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
