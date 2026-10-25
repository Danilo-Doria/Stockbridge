package com.stockbridge.inventory.domain.model;

import com.stockbridge.inventory.domain.exception.InvalidRequestException;
import com.stockbridge.inventory.domain.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class RequestTest {

    private Request pending() { return Request.create(1L, 10L, 5, "Para mi equipo"); }

    @Test
    @DisplayName("Una solicitud nueva nace PENDIENTE y sin id")
    void createsPending() {
        Request r = pending();
        assertNull(r.getId());
        assertEquals(RequestStatus.PENDIENTE, r.getStatus());
        assertNotNull(r.getCreatedAt());
    }

    @Test
    @DisplayName("Rechaza cantidad cero o negativa")
    void rejectsInvalidQuantity() {
        assertThrows(InvalidRequestException.class, () -> Request.create(1L, 10L, 0, null));
        assertThrows(InvalidRequestException.class, () -> Request.create(1L, 10L, -3, null));
    }

    @Test
    @DisplayName("Rechaza usuario o producto nulos")
    void rejectsNullReferences() {
        assertThrows(InvalidRequestException.class, () -> Request.create(null, 10L, 1, null));
        assertThrows(InvalidRequestException.class, () -> Request.create(1L, null, 1, null));
    }

    @Test
    @DisplayName("Flujo feliz: PENDIENTE -> APROBADA -> ENTREGADA")
    void happyPath() {
        Request r = pending();
        r.approve();
        assertEquals(RequestStatus.APROBADA, r.getStatus());
        r.deliver();
        assertEquals(RequestStatus.ENTREGADA, r.getStatus());
    }

    @Test
    @DisplayName("No se puede entregar una solicitud PENDIENTE")
    void cannotDeliverPending() {
        Request r = pending();
        assertThrows(InvalidStateTransitionException.class, r::deliver);
        assertEquals(RequestStatus.PENDIENTE, r.getStatus());
    }

    @Test
    @DisplayName("Rechazar exige motivo y lo guarda en la observación")
    void rejectRequiresReason() {
        Request r = pending();
        assertThrows(InvalidRequestException.class, () -> r.reject("  "));
        assertEquals(RequestStatus.PENDIENTE, r.getStatus());

        r.reject("Sin presupuesto");
        assertEquals(RequestStatus.RECHAZADA, r.getStatus());
        assertEquals("Sin presupuesto", r.getObservation());
    }

    @Test
    @DisplayName("Se puede cancelar desde PENDIENTE y desde APROBADA")
    void cancelFromPendingAndApproved() {
        Request a = pending();
        a.cancel();
        assertEquals(RequestStatus.CANCELADA, a.getStatus());

        Request b = pending();
        b.approve();
        b.cancel();
        assertEquals(RequestStatus.CANCELADA, b.getStatus());
    }

    @Test
    @DisplayName("Una solicitud ENTREGADA no se puede cancelar")
    void cannotCancelDelivered() {
        Request r = pending();
        r.approve();
        r.deliver();
        assertThrows(InvalidStateTransitionException.class, r::cancel);
    }

    @ParameterizedTest
    @EnumSource(value = RequestStatus.class, names = {"RECHAZADA", "ENTREGADA", "CANCELADA"})
    @DisplayName("Los estados finales no pueden pasar a ningún otro")
    void finalStatesAreTerminal(RequestStatus finalStatus) {
        for (RequestStatus target : RequestStatus.values()) {
            assertFalse(finalStatus.canTransitionTo(target),
                    finalStatus + " no debería poder pasar a " + target);
        }
    }

    @Test
    @DisplayName("isOwnedBy compara con el dueño")
    void ownership() {
        Request r = pending();
        assertTrue(r.isOwnedBy(1L));
        assertFalse(r.isOwnedBy(2L));
    }

    @Test
    @DisplayName("appendObservation concatena y respeta el límite de 500")
    void appendsObservation() {
        Request r = pending();
        r.appendObservation("Nota del proveedor");
        assertEquals("Para mi equipo | Nota del proveedor", r.getObservation());

        r.appendObservation("x".repeat(600));
        assertEquals(500, r.getObservation().length());
    }
}
