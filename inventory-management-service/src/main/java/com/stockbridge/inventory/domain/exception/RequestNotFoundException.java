package com.stockbridge.inventory.domain.exception;

public class RequestNotFoundException extends DomainException {
    public RequestNotFoundException(Long id) { super("Solicitud no encontrada: " + id); }
}
