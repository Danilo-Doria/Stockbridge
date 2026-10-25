package com.stockbridge.inventory.domain.exception;

public class SupplierUnavailableException extends RuntimeException {
    public SupplierUnavailableException(String message, Throwable cause) { super(message, cause); }
}
