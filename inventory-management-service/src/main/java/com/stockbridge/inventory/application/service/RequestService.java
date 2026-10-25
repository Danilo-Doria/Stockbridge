package com.stockbridge.inventory.application.service;

import com.stockbridge.inventory.application.common.PageResult;
import com.stockbridge.inventory.application.common.RequestView;
import com.stockbridge.inventory.application.port.in.CreateRequestUseCase;
import com.stockbridge.inventory.application.port.in.GetRequestUseCase;
import com.stockbridge.inventory.application.port.in.ListRequestsUseCase;
import com.stockbridge.inventory.application.port.in.ManageRequestUseCase;
import com.stockbridge.inventory.application.port.out.MovementRepositoryPort;
import com.stockbridge.inventory.application.port.out.ProductRepositoryPort;
import com.stockbridge.inventory.application.port.out.RequestRepositoryPort;
import com.stockbridge.inventory.application.port.out.SupplierAvailabilityPort;
import com.stockbridge.inventory.domain.exception.InactiveProductException;
import com.stockbridge.inventory.domain.exception.InsufficientStockException;
import com.stockbridge.inventory.domain.exception.ProductNotFoundException;
import com.stockbridge.inventory.domain.exception.RequestNotFoundException;
import com.stockbridge.inventory.domain.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RequestService implements CreateRequestUseCase, GetRequestUseCase,
        ListRequestsUseCase, ManageRequestUseCase {

    private final RequestRepositoryPort requests;
    private final ProductRepositoryPort products;
    private final MovementRepositoryPort movements;
    private final SupplierAvailabilityPort supplierAvailability;

    public RequestService(MovementRepositoryPort movements, RequestRepositoryPort requests, ProductRepositoryPort products, SupplierAvailabilityPort supplierAvailability) {
        this.movements = movements;
        this.requests = requests;
        this.products = products;
        this.supplierAvailability = supplierAvailability;
    }

    @Override
    public RequestView create(CreateRequestCommand c) {
        Request request = Request.create(c.userId(), c.productId(), c.quantity(), c.observation());
        Product product = products.findById(c.productId())
                .orElseThrow(() -> new ProductNotFoundException(c.productId()));
        if (!product.isActive()) throw new InactiveProductException(product.getCode());

        if (!product.hasEnoughStock(c.quantity())) {
            SupplierAvailabilityPort.SupplierAvailability s = supplierAvailability.check(product.getCode(), c.quantity(), product.getCategory());
            if (!s.available()) {
                throw new InsufficientStockException("Stock insuficiente para " + product.getCode()
                        + " (local: " + product.getStock() + ", proveedor: " + s.availableQuantity()
                        + ", solicitado: " + c.quantity() + ")");
            }
            request.appendObservation("Sin stock local; proveedor " + s.supplierCode()
                    + " tiene " + s.availableQuantity() + " u., entrega estimada en "
                    + s.estimatedDeliveryDays() + " días");
        }
        return view(requests.save(request).getId());
    }

    @Override
    @Transactional(readOnly = true)
    public RequestView getById(Long id, Long requesterId, Role role) {
        RequestView v = requests.findViewById(id).orElseThrow(() -> new RequestNotFoundException(id));
        checkAccess(v.request(), requesterId, role);
        return v;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<RequestView> listMine(Long userId, int page, int size) {
        return requests.findViewsByUser(userId, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<RequestView> listAll(RequestStatus status, int page, int size) {
        return requests.findViews(status, page, size);
    }

    @Override
    public RequestView approve(Long id) {
        Request r = load(id);
        r.approve();
        requests.save(r);
        return view(id);
    }

    @Override
    public RequestView reject(Long id, String reason) {
        Request r = load(id);
        r.reject(reason);
        requests.save(r);
        return view(id);
    }

    @Override
    public RequestView cancel(Long id, Long requesterId, Role role) {
        Request r = load(id);
        checkAccess(r, requesterId, role);
        r.cancel();
        requests.save(r);
        return view(id);
    }

    /**
     * Una sola transacción: estado + stock + movimiento. Si algo falla, se revierte.
     */
    @Override
    public RequestView deliver(Long id, Long performedBy) {
        Request r = load(id);
        Product p = products.findByIdForUpdate(r.getProductId())      // bloquea la fila del producto
                .orElseThrow(() -> new ProductNotFoundException(r.getProductId()));

        r.deliver();                              // falla si no está APROBADA
        p.decreaseStock(r.getQuantity());         // falla si no hay stock

        products.save(p);
        requests.save(r);
        movements.save(InventoryMovement.outbound(p.getId(), r.getId(), performedBy, r.getQuantity()));
        return view(id);
    }

    private Request load(Long id) {
        return requests.findById(id).orElseThrow(() -> new RequestNotFoundException(id));
    }

    private RequestView view(Long id) {
        return requests.findViewById(id).orElseThrow(() -> new RequestNotFoundException(id));
    }

    /**
     * Un empleado solo accede a lo suyo. Si no es suyo, responde "no existe" para no revelar el id.
     */
    private void checkAccess(Request r, Long requesterId, Role role) {
        if (role == Role.EMPLEADO && !r.isOwnedBy(requesterId)) {
            throw new RequestNotFoundException(r.getId());
        }
    }
}