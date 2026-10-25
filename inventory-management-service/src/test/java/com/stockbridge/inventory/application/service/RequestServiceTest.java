package com.stockbridge.inventory.application.service;

import com.stockbridge.inventory.application.common.RequestView;
import com.stockbridge.inventory.application.port.in.CreateRequestUseCase.CreateRequestCommand;
import com.stockbridge.inventory.application.port.out.*;
import com.stockbridge.inventory.application.port.out.SupplierAvailabilityPort.SupplierAvailability;
import com.stockbridge.inventory.domain.exception.*;
import com.stockbridge.inventory.domain.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {

    @Mock RequestRepositoryPort requests;
    @Mock ProductRepositoryPort products;
    @Mock MovementRepositoryPort movements;
    @Mock SupplierAvailabilityPort supplier;
    @InjectMocks RequestService service;

    // ---------- helpers ----------
    private Product product(int stock, boolean active) {
        return Product.restore(10L, "MON-001", "Monitor", null, "MONITORES",
                new BigDecimal("350.00"), stock, active);
    }

    private Request stored(RequestStatus status, int qty) {
        return Request.restore(1L, 2L, 10L, qty, LocalDateTime.now(), status, null);
    }

    private RequestView viewOf(Request r) {
        return new RequestView(r, "Ana", "MON-001", "Monitor");
    }

    // =====================================================
    @Nested
    @DisplayName("deliver (entrega transaccional)")
    class Deliver {

        @Test
        @DisplayName("Éxito: descuenta stock, entrega y registra el movimiento")
        void deliversAndRecordsMovement() {
            Request r = stored(RequestStatus.APROBADA, 5);
            Product p = product(10, true);
            when(requests.findById(1L)).thenReturn(Optional.of(r));
            when(products.findByIdForUpdate(10L)).thenReturn(Optional.of(p));
            when(requests.findViewById(1L)).thenReturn(Optional.of(viewOf(r)));

            service.deliver(1L, 3L);

            assertEquals(5, p.getStock());
            assertEquals(RequestStatus.ENTREGADA, r.getStatus());
            verify(products).save(p);
            verify(requests).save(r);

            ArgumentCaptor<InventoryMovement> captor = ArgumentCaptor.forClass(InventoryMovement.class);
            verify(movements).save(captor.capture());
            InventoryMovement m = captor.getValue();
            assertEquals(MovementType.SALIDA, m.getType());
            assertEquals(5, m.getQuantity());
            assertEquals(3L, m.getPerformedBy());
            assertEquals(1L, m.getRequestId());
        }

        @Test
        @DisplayName("Stock insuficiente: lanza excepción y no guarda nada")
        void failsWithoutStock() {
            Request r = stored(RequestStatus.APROBADA, 5);
            Product p = product(3, true);
            when(requests.findById(1L)).thenReturn(Optional.of(r));
            when(products.findByIdForUpdate(10L)).thenReturn(Optional.of(p));

            assertThrows(InsufficientStockException.class, () -> service.deliver(1L, 3L));

            assertEquals(3, p.getStock());
            verify(products, never()).save(any());
            verify(requests, never()).save(any());
            verify(movements, never()).save(any());
        }

        @Test
        @DisplayName("Solicitud no aprobada: transición inválida y no toca el stock")
        void failsIfNotApproved() {
            Request r = stored(RequestStatus.PENDIENTE, 5);
            Product p = product(10, true);
            when(requests.findById(1L)).thenReturn(Optional.of(r));
            when(products.findByIdForUpdate(10L)).thenReturn(Optional.of(p));

            assertThrows(InvalidStateTransitionException.class, () -> service.deliver(1L, 3L));

            assertEquals(10, p.getStock());
            verify(movements, never()).save(any());
        }

        @Test
        @DisplayName("Solicitud inexistente: RequestNotFoundException")
        void failsIfRequestMissing() {
            when(requests.findById(99L)).thenReturn(Optional.empty());

            assertThrows(RequestNotFoundException.class, () -> service.deliver(99L, 3L));
            verifyNoInteractions(movements);
        }
    }

    // =====================================================
    @Nested
    @DisplayName("create (con proveedor externo simulado)")
    class Create {

        private final CreateRequestCommand cmd = new CreateRequestCommand(2L, 10L, 5, "Urgente");

        private void stubSave() {
            when(requests.save(any(Request.class))).thenAnswer(inv -> {
                Request in = inv.getArgument(0);
                return Request.restore(1L, in.getUserId(), in.getProductId(), in.getQuantity(),
                        in.getCreatedAt(), in.getStatus(), in.getObservation());
            });
            when(requests.findViewById(1L)).thenAnswer(inv -> Optional.of(viewOf(stored(RequestStatus.PENDIENTE, 5))));
        }

        @Test
        @DisplayName("Con stock local suficiente no consulta al proveedor")
        void enoughLocalStock() {
            when(products.findById(10L)).thenReturn(Optional.of(product(10, true)));
            stubSave();

            service.create(cmd);

            verifyNoInteractions(supplier);
            verify(requests).save(any(Request.class));
        }

        @Test
        @DisplayName("Sin stock local pero el proveedor tiene: crea PENDIENTE con nota")
        void supplierHasStock() {
            when(products.findById(10L)).thenReturn(Optional.of(product(1, true)));
            when(supplier.check("MON-001", 5, "MONITORES"))
                    .thenReturn(new SupplierAvailability("SUP-002", true, 32, 1));
            stubSave();

            service.create(cmd);

            ArgumentCaptor<Request> captor = ArgumentCaptor.forClass(Request.class);
            verify(requests).save(captor.capture());
            assertEquals(RequestStatus.PENDIENTE, captor.getValue().getStatus());
            assertTrue(captor.getValue().getObservation().contains("SUP-002"));
        }

        @Test
        @DisplayName("Ni local ni proveedor: InsufficientStockException y no se guarda")
        void supplierHasNoStock() {
            when(products.findById(10L)).thenReturn(Optional.of(product(1, true)));
            when(supplier.check("MON-001", 5, "MONITORES"))
                    .thenReturn(new SupplierAvailability("SUP-001", false, 3, 6));

            assertThrows(InsufficientStockException.class, () -> service.create(cmd));
            verify(requests, never()).save(any());
        }

        @Test
        @DisplayName("Proveedor caído: la excepción sube y no se crea la solicitud")
        void supplierDown() {
            when(products.findById(10L)).thenReturn(Optional.of(product(1, true)));
            when(supplier.check(any(), anyInt(), any()))
                    .thenThrow(new SupplierUnavailableException("caído", null));

            assertThrows(SupplierUnavailableException.class, () -> service.create(cmd));
            verify(requests, never()).save(any());
        }

        @Test
        @DisplayName("Producto inactivo: no se puede solicitar ni se consulta al proveedor")
        void inactiveProduct() {
            when(products.findById(10L)).thenReturn(Optional.of(product(10, false)));

            assertThrows(InactiveProductException.class, () -> service.create(cmd));
            verifyNoInteractions(supplier);
            verify(requests, never()).save(any());
        }

        @Test
        @DisplayName("Producto inexistente: ProductNotFoundException")
        void productMissing() {
            when(products.findById(10L)).thenReturn(Optional.empty());

            assertThrows(ProductNotFoundException.class, () -> service.create(cmd));
        }

        @Test
        @DisplayName("Cantidad inválida: se rechaza antes de tocar la base")
        void invalidQuantity() {
            assertThrows(InvalidRequestException.class,
                    () -> service.create(new CreateRequestCommand(2L, 10L, 0, null)));
            verifyNoInteractions(products, supplier);
        }
    }

    // =====================================================
    @Nested
    @DisplayName("Control de propiedad")
    class Ownership {

        @Test
        @DisplayName("Un empleado no puede ver la solicitud de otro (404)")
        void employeeCannotSeeOthers() {
            Request r = stored(RequestStatus.PENDIENTE, 5);   // dueño: usuario 2
            when(requests.findViewById(1L)).thenReturn(Optional.of(viewOf(r)));

            assertThrows(RequestNotFoundException.class,
                    () -> service.getById(1L, 99L, Role.EMPLEADO));
        }

        @Test
        @DisplayName("El dueño sí puede verla")
        void ownerCanSee() {
            Request r = stored(RequestStatus.PENDIENTE, 5);
            when(requests.findViewById(1L)).thenReturn(Optional.of(viewOf(r)));

            assertEquals(1L, service.getById(1L, 2L, Role.EMPLEADO).request().getId());
        }

        @Test
        @DisplayName("BODEGA puede ver cualquier solicitud")
        void warehouseCanSeeAny() {
            Request r = stored(RequestStatus.PENDIENTE, 5);
            when(requests.findViewById(1L)).thenReturn(Optional.of(viewOf(r)));

            assertDoesNotThrow(() -> service.getById(1L, 99L, Role.BODEGA));
        }

        @Test
        @DisplayName("Un empleado no puede cancelar la solicitud de otro")
        void employeeCannotCancelOthers() {
            Request r = stored(RequestStatus.PENDIENTE, 5);
            when(requests.findById(1L)).thenReturn(Optional.of(r));

            assertThrows(RequestNotFoundException.class,
                    () -> service.cancel(1L, 99L, Role.EMPLEADO));
            assertEquals(RequestStatus.PENDIENTE, r.getStatus());
            verify(requests, never()).save(any());
        }
    }
}