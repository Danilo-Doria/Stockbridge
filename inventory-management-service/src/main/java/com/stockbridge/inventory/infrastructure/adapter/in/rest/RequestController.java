package com.stockbridge.inventory.infrastructure.adapter.in.rest;

import com.stockbridge.inventory.application.common.PageResult;
import com.stockbridge.inventory.application.common.RequestView;
import com.stockbridge.inventory.application.port.in.CreateRequestUseCase;
import com.stockbridge.inventory.application.port.in.GetRequestUseCase;
import com.stockbridge.inventory.application.port.in.ListRequestsUseCase;
import com.stockbridge.inventory.application.port.in.ManageRequestUseCase;
import com.stockbridge.inventory.domain.model.RequestStatus;
import com.stockbridge.inventory.infrastructure.config.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/requests")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Solicitudes", description = "Solicitudes internas de productos. "
        + "Flujo: PENDIENTE → APROBADA → ENTREGADA (también RECHAZADA o CANCELADA)")
public class RequestController {

    private final CreateRequestUseCase create;
    private final GetRequestUseCase get;
    private final ListRequestsUseCase list;
    private final ManageRequestUseCase manage;

    public RequestController(CreateRequestUseCase create, GetRequestUseCase get,
                             ListRequestsUseCase list, ManageRequestUseCase manage) {
        this.create = create; this.get = get; this.list = list; this.manage = manage;
    }

    @Operation(summary = "Crear solicitud",
            description = "Cualquier usuario autenticado. El solicitante sale del token. "
                    + "Si no hay stock local, consulta al proveedor externo: si este tiene disponibilidad, "
                    + "la solicitud se crea PENDIENTE con una nota; si no, responde 409.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Solicitud creada en estado PENDIENTE"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "409", description = "Stock insuficiente local y en el proveedor"),
            @ApiResponse(responseCode = "422", description = "Producto inactivo"),
            @ApiResponse(responseCode = "503", description = "Proveedor externo no disponible o con timeout")
    })
    @PostMapping
    public ResponseEntity<RequestResponse> create(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateRequestRequest r) {
        RequestView v = create.create(new CreateRequestUseCase.CreateRequestCommand(
                user.id(), r.productId(), r.quantity(), r.observation()));
        return ResponseEntity.created(URI.create("/api/requests/" + v.request().getId()))
                .body(RequestResponse.from(v));
    }

    @Operation(summary = "Mis solicitudes", description = "Lista solo las solicitudes del usuario autenticado, "
            + "de la más reciente a la más antigua.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de solicitudes propias"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido")
    })
    @GetMapping("/mine")
    public PageResult<RequestResponse> mine(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "Número de página (desde 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "10") int size) {
        return list.listMine(user.id(), page, size).map(RequestResponse::from);
    }

    @Operation(summary = "Listar todas las solicitudes",
            description = "Requiere rol BODEGA o ADMIN. Permite filtrar por estado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de solicitudes"),
            @ApiResponse(responseCode = "403", description = "Sin rol BODEGA o ADMIN")
    })
    @GetMapping
    public PageResult<RequestResponse> all(
            @Parameter(description = "Filtrar por estado (opcional)") @RequestParam(required = false) RequestStatus status,
            @Parameter(description = "Número de página (desde 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "10") int size) {
        return list.listAll(status, page, size).map(RequestResponse::from);
    }

    @Operation(summary = "Consultar solicitud por id",
            description = "Un EMPLEADO solo puede ver las suyas: si pide la de otro recibe 404, "
                    + "para no revelar que ese id existe. BODEGA y ADMIN ven cualquiera.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe o no pertenece al usuario")
    })
    @GetMapping("/{id}")
    public RequestResponse get(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "Id de la solicitud") @PathVariable Long id) {
        return RequestResponse.from(get.getById(id, user.id(), user.role()));
    }

    @Operation(summary = "Aprobar solicitud", description = "Requiere BODEGA o ADMIN. PENDIENTE → APROBADA.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud aprobada"),
            @ApiResponse(responseCode = "403", description = "Sin rol BODEGA o ADMIN"),
            @ApiResponse(responseCode = "404", description = "Solicitud no encontrada"),
            @ApiResponse(responseCode = "409", description = "Transición de estado inválida")
    })
    @PatchMapping("/{id}/approve")
    public RequestResponse approve(@Parameter(description = "Id de la solicitud") @PathVariable Long id) {
        return RequestResponse.from(manage.approve(id));
    }

    @Operation(summary = "Rechazar solicitud",
            description = "Requiere BODEGA o ADMIN. PENDIENTE → RECHAZADA. El motivo es obligatorio y queda en la observación.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud rechazada"),
            @ApiResponse(responseCode = "400", description = "Falta el motivo"),
            @ApiResponse(responseCode = "403", description = "Sin rol BODEGA o ADMIN"),
            @ApiResponse(responseCode = "404", description = "Solicitud no encontrada"),
            @ApiResponse(responseCode = "409", description = "Transición de estado inválida")
    })
    @PatchMapping("/{id}/reject")
    public RequestResponse reject(@Parameter(description = "Id de la solicitud") @PathVariable Long id,
                                  @Valid @RequestBody RejectRequestRequest r) {
        return RequestResponse.from(manage.reject(id, r.reason()));
    }

    @Operation(summary = "Cancelar solicitud",
            description = "Un EMPLEADO solo cancela las suyas; BODEGA y ADMIN, cualquiera. "
                    + "Posible desde PENDIENTE o APROBADA.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud cancelada"),
            @ApiResponse(responseCode = "404", description = "No existe o no pertenece al usuario"),
            @ApiResponse(responseCode = "409", description = "Transición de estado inválida")
    })
    @PatchMapping("/{id}/cancel")
    public RequestResponse cancel(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "Id de la solicitud") @PathVariable Long id) {
        return RequestResponse.from(manage.cancel(id, user.id(), user.role()));
    }

    @Operation(summary = "Entregar solicitud",
            description = "Requiere BODEGA o ADMIN. APROBADA → ENTREGADA. En una sola transacción descuenta "
                    + "el stock y registra el movimiento de salida; si algo falla, todo se revierte. "
                    + "El producto se bloquea durante la operación para evitar stock negativo por concurrencia.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entrega registrada"),
            @ApiResponse(responseCode = "403", description = "Sin rol BODEGA o ADMIN"),
            @ApiResponse(responseCode = "404", description = "Solicitud o producto no encontrado"),
            @ApiResponse(responseCode = "409", description = "No está APROBADA o el stock es insuficiente")
    })
    @PatchMapping("/{id}/deliver")
    public RequestResponse deliver(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "Id de la solicitud") @PathVariable Long id) {
        return RequestResponse.from(manage.deliver(id, user.id()));
    }
}
