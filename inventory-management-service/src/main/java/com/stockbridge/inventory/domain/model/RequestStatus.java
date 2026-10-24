package com.stockbridge.inventory.domain.model;

public enum RequestStatus {
    PENDIENTE, APROBADA, RECHAZADA, ENTREGADA, CANCELADA;

    public boolean canTransitionTo(RequestStatus target) {
        return switch (this) {
            case PENDIENTE -> target == APROBADA || target == RECHAZADA || target == CANCELADA;
            case APROBADA  -> target == ENTREGADA || target == CANCELADA;
            case RECHAZADA, ENTREGADA, CANCELADA -> false;
        };
    }
}
