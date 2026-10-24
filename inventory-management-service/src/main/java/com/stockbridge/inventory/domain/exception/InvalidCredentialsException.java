package com.stockbridge.inventory.domain.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() { super("Credenciales inválidas"); }
}
