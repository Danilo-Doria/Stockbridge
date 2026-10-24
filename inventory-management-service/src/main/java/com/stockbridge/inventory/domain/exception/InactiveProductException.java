package com.stockbridge.inventory.domain.exception;

public class InactiveProductException extends DomainException {
    public InactiveProductException(String code) { super("El producto " + code + " está inactivo y no puede solicitarse"); }
}
