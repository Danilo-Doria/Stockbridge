package com.stockbridge.inventory.domain.exception;

public class InvalidCredentialsException extends DomainException {
    public InvalidCredentialsException() { super("Credenciales inválidas"); }
}
