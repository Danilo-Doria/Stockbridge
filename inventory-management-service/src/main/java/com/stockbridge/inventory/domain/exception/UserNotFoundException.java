package com.stockbridge.inventory.domain.exception;

public class UserNotFoundException extends DomainException {
    public UserNotFoundException(Long id) { super("Usuario no encontrado: " + id); }
}
