package com.stockbridge.inventory.domain.exception;

public class DuplicateProductCodeException extends DomainException {
    public DuplicateProductCodeException(String code) { super("Ya existe un producto con el código " + code); }
}
