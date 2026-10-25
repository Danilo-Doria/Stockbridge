package com.stockbridge.inventory.infrastructure.adapter.in.rest;

import com.stockbridge.inventory.application.common.PageResult;
import com.stockbridge.inventory.application.port.in.ListMovementsUseCase;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/movements")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Movimientos de inventario", description = "Trazabilidad de entradas y salidas. Requiere BODEGA o ADMIN")
public class MovementController {
    private final ListMovementsUseCase movements;
    public MovementController(ListMovementsUseCase movements) { this.movements = movements; }

    @Operation(summary = "Movimientos de un producto",
            description = "Historial paginado, del más reciente al más antiguo. Incluye quién lo realizó "
                    + "y la solicitud asociada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de movimientos"),
            @ApiResponse(responseCode = "403", description = "Sin rol BODEGA o ADMIN")
    })
    @GetMapping
    public PageResult<MovementResponse> byProduct(
            @Parameter(description = "Id del producto", required = true) @RequestParam Long productId,
            @Parameter(description = "Número de página (desde 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "10") int size) {
        return movements.byProduct(productId, page, size).map(MovementResponse::from);
    }
}
