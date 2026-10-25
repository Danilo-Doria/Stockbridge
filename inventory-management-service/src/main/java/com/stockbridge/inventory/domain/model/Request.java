package com.stockbridge.inventory.domain.model;

import com.stockbridge.inventory.domain.exception.InvalidRequestException;
import com.stockbridge.inventory.domain.exception.InvalidStateTransitionException;

import java.time.LocalDateTime;

public class Request {

    private final Long id;
    private final Long userId;
    private final Long productId;
    private final int quantity;
    private final LocalDateTime createdAt;
    private RequestStatus status;
    private String observation;

    private Request(Long id, Long userId, Long productId, int quantity,
                    LocalDateTime createdAt, RequestStatus status, String observation) {
        if (userId == null) throw new InvalidRequestException("El usuario solicitante es obligatorio");
        if (productId == null) throw new InvalidRequestException("El producto es obligatorio");
        if (quantity <= 0) throw new InvalidRequestException("La cantidad debe ser mayor que cero");
        if (status == null || createdAt == null)
            throw new InvalidRequestException("Estado y fecha son obligatorios");
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.createdAt = createdAt;
        this.status = status;
        this.observation = observation;
    }

    public static Request create(Long userId, Long productId, int quantity, String observation) {
        return new Request(null, userId, productId, quantity,
                LocalDateTime.now(), RequestStatus.PENDIENTE, observation);
    }

    public static Request restore(Long id, Long userId, Long productId, int quantity,
                                  LocalDateTime createdAt, RequestStatus status, String observation) {
        if (id == null) throw new InvalidRequestException("El id es obligatorio al reconstruir");
        return new Request(id, userId, productId, quantity, createdAt, status, observation);
    }

    public void approve() {
        transitionTo(RequestStatus.APROBADA);
    }

    public void reject(String reason) {
        if (reason == null || reason.isBlank())
            throw new InvalidRequestException("Debe indicar el motivo del rechazo");
        transitionTo(RequestStatus.RECHAZADA);
        this.observation = reason;
    }

    public void deliver() {
        transitionTo(RequestStatus.ENTREGADA);
    }

    public void cancel() {
        transitionTo(RequestStatus.CANCELADA);
    }

    private void transitionTo(RequestStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new InvalidStateTransitionException(
                    "No se puede pasar de " + status + " a " + target);
        }
        this.status = target;
    }

    public void appendObservation(String note) {
        if (note == null || note.isBlank()) return;
        String merged = (observation == null || observation.isBlank()) ? note : observation + " | " + note;
        this.observation = merged.length() > 500 ? merged.substring(0, 500) : merged;
    }

    public boolean isOwnedBy(Long candidateUserId) {
        return userId.equals(candidateUserId);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getObservation() {
        return observation;
    }

    public Long getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public Long getUserId() {
        return userId;
    }
}