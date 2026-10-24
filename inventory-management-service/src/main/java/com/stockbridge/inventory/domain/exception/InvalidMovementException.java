package com.stockbridge.inventory.domain.exception;

public class InvalidMovementException extends DomainException {
    public InvalidMovementException(String message) {
        super(message);
    }
}
