package com.stockbridge.inventory.domain.exception;

public class InvalidUserException extends DomainException {
    public InvalidUserException(String message) {
        super(message);
    }
}
