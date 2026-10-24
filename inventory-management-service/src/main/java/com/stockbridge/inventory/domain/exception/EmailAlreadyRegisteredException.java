package com.stockbridge.inventory.domain.exception;

public class EmailAlreadyRegisteredException extends DomainException {
    public EmailAlreadyRegisteredException(String email) { super("El email ya está registrado: " + email); }
}
