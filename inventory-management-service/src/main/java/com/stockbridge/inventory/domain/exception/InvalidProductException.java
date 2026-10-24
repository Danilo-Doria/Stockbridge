package com.stockbridge.inventory.domain.exception;

public class InvalidProductException extends RuntimeException {
    public InvalidProductException(String message) { super(message); }
}
