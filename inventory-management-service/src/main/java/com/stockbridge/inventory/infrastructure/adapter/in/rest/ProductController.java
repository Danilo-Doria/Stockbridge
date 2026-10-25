package com.stockbridge.inventory.infrastructure.adapter.in.rest;

import com.stockbridge.inventory.application.common.PageResult;
import com.stockbridge.inventory.application.port.in.*;
import com.stockbridge.inventory.domain.model.Product;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/products")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Productos", description = "Catálogo e inventario. Consulta: cualquier usuario autenticado. Escritura: ADMIN")
public class ProductController {

    private final CreateProductUseCase create;
    private final UpdateProductUseCase update;
    private final GetProductUseCase get;
    private final ListProductsUseCase list;
    private final ChangeProductStatusUseCase status;

    public ProductController(CreateProductUseCase create, UpdateProductUseCase update,
                             GetProductUseCase get, ListProductsUseCase list,
                             ChangeProductStatusUseCase status) {
        this.create = create;
        this.update = update;
        this.get = get;
        this.list = list;
        this.status = status;
    }

    @Operation(summary = "Crear producto", description = "Requiere rol ADMIN. El código debe ser único, "
            + "el precio mayor que cero y el stock no negativo.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Producto creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido"),
            @ApiResponse(responseCode = "403", description = "Sin rol ADMIN"),
            @ApiResponse(responseCode = "409", description = "Código de producto duplicado")
    })
    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest r) {
        Product p = create.create(new CreateProductUseCase.CreateProductCommand(
                r.code(), r.name(), r.description(), r.category(), r.price(), r.stock()));
        return ResponseEntity.created(URI.create("/api/products/" + p.getId()))
                .body(ProductResponse.from(p));
    }

    @Operation(summary = "Listar productos", description = "Paginado y ordenado por nombre.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de productos"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido")
    })
    @GetMapping
    public PageResult<ProductResponse> list(
            @Parameter(description = "Número de página (desde 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "10") int size) {
        return list.list(page, size).map(ProductResponse::from);
    }

    @Operation(summary = "Productos con bajo stock",
            description = "Devuelve los productos cuyo stock es menor al mínimo indicado, del más bajo al más alto.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de productos con stock bajo"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido")
    })
    @GetMapping("/low-stock")
    public PageResult<ProductResponse> lowStock(
            @Parameter(description = "Stock mínimo: se listan los que están por debajo", example = "20")
            @RequestParam int minimum,
            @Parameter(description = "Número de página (desde 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "10") int size) {
        return list.listLowStock(minimum, page, size).map(ProductResponse::from);
    }

    @Operation(summary = "Consultar producto por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/{id}")
    public ProductResponse get(@Parameter(description = "Id del producto") @PathVariable Long id) {
        return ProductResponse.from(get.getById(id));
    }

    @Operation(summary = "Actualizar producto",
            description = "Requiere rol ADMIN. Modifica nombre, descripción, categoría y precio. "
                    + "El código y el stock no se cambian aquí.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "Sin rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PutMapping("/{id}")
    public ProductResponse update(@Parameter(description = "Id del producto") @PathVariable Long id,
                                  @Valid @RequestBody UpdateProductRequest r) {
        return ProductResponse.from(update.update(id,
                new UpdateProductUseCase.UpdateProductCommand(r.name(), r.description(), r.category(), r.price())));
    }

    @Operation(summary = "Activar producto", description = "Requiere rol ADMIN. Un producto activo puede solicitarse.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto activado"),
            @ApiResponse(responseCode = "403", description = "Sin rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PatchMapping("/{id}/activate")
    public ProductResponse activate(@Parameter(description = "Id del producto") @PathVariable Long id) {
        return ProductResponse.from(status.activate(id));
    }

    @Operation(summary = "Desactivar producto",
            description = "Requiere rol ADMIN. Un producto inactivo no puede solicitarse.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto desactivado"),
            @ApiResponse(responseCode = "403", description = "Sin rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PatchMapping("/{id}/deactivate")
    public ProductResponse deactivate(@Parameter(description = "Id del producto") @PathVariable Long id) {
        return ProductResponse.from(status.deactivate(id));
    }
}
