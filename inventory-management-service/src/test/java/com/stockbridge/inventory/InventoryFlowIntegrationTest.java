package com.stockbridge.inventory;

import com.jayway.jsonpath.JsonPath;
import com.stockbridge.inventory.application.port.out.MovementRepositoryPort;
import com.stockbridge.inventory.application.port.out.SupplierAvailabilityPort;
import com.stockbridge.inventory.domain.exception.SupplierUnavailableException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// IMPORTANTE: deja que el IDE importe @AutoConfigureMockMvc (en Boot 4 cambió de paquete)
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class InventoryFlowIntegrationTest {

    private static final MediaType JSON = MediaType.APPLICATION_JSON;

    @Autowired MockMvc mvc;

    @MockitoBean SupplierAvailabilityPort supplier;        // el proveedor externo se simula
    @MockitoSpyBean MovementRepositoryPort movements;      // espía: permite forzar un fallo

    // ---------------- helpers ----------------
    private record Person(long id, String token) {}

    private String body(ResultActions r) throws Exception {
        return r.andReturn().getResponse().getContentAsString();
    }

    private String login(String email, String password) throws Exception {
        String res = body(mvc.perform(post("/auth/login").contentType(JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk()));
        return JsonPath.read(res, "$.token");
    }

    private String adminToken() throws Exception {
        return login("admin@gmail.com", "admin123");   // lo crea AdminBootstrap con los valores por defecto
    }

    private Person employee() throws Exception {
        String email = "emp-" + UUID.randomUUID() + "@x.com";
        String res = body(mvc.perform(post("/auth/register").contentType(JSON)
                        .content("{\"fullName\":\"Empleado\",\"email\":\"" + email + "\",\"password\":\"12345678\"}"))
                .andExpect(status().isCreated()));
        Number id = JsonPath.read(res, "$.id");
        return new Person(id.longValue(), login(email, "12345678"));
    }

    private Person warehouse() throws Exception {
        String email = "bod-" + UUID.randomUUID() + "@x.com";
        String res = body(mvc.perform(post("/auth/register").contentType(JSON)
                        .content("{\"fullName\":\"Bodega\",\"email\":\"" + email + "\",\"password\":\"12345678\"}"))
                .andExpect(status().isCreated()));
        Number id = JsonPath.read(res, "$.id");
        mvc.perform(patch("/api/users/" + id + "/role")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(JSON).content("{\"role\":\"BODEGA\"}"))
                .andExpect(status().isOk());
        return new Person(id.longValue(), login(email, "12345678"));   // login DESPUÉS del cambio de rol
    }

    private long product(int stock) throws Exception {
        String code = "P-" + UUID.randomUUID().toString().substring(0, 8);
        String res = body(mvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + adminToken()).contentType(JSON)
                        .content("{\"code\":\"" + code + "\",\"name\":\"Item\",\"category\":\"CAT\","
                                + "\"price\":10,\"stock\":" + stock + "}"))
                .andExpect(status().isCreated()));
        Number id = JsonPath.read(res, "$.id");
        return id.longValue();
    }

    private long request(Person who, long productId, int qty) throws Exception {
        String res = body(mvc.perform(post("/api/requests")
                        .header("Authorization", "Bearer " + who.token()).contentType(JSON)
                        .content("{\"productId\":" + productId + ",\"quantity\":" + qty + "}"))
                .andExpect(status().isCreated()));
        Number id = JsonPath.read(res, "$.id");
        return id.longValue();
    }

    private void patchAs(Person who, String path) throws Exception {
        mvc.perform(patch(path).header("Authorization", "Bearer " + who.token()))
                .andExpect(status().isOk());
    }

    // ---------------- seguridad ----------------
    @Test
    @DisplayName("Sin token: 401")
    void noTokenIs401() throws Exception {
        mvc.perform(get("/api/products")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Token manipulado: 401")
    void tamperedTokenIs401() throws Exception {
        String token = employee().token();
        String[] parts = token.split("\\.");
        String bad = parts[0] + ".XXXX" + parts[1].substring(4) + "." + parts[2];
        mvc.perform(get("/api/products").header("Authorization", "Bearer " + bad))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Un empleado no puede crear productos (403) pero sí consultarlos (200)")
    void employeeCannotCreateProducts() throws Exception {
        Person ana = employee();
        mvc.perform(post("/api/products").header("Authorization", "Bearer " + ana.token())
                        .contentType(JSON)
                        .content("{\"code\":\"X1\",\"name\":\"X\",\"category\":\"C\",\"price\":1,\"stock\":1}"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/products").header("Authorization", "Bearer " + ana.token()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Un empleado no puede aprobar ni listar todas las solicitudes (403)")
    void employeeCannotManage() throws Exception {
        Person ana = employee();
        long rid = request(ana, product(10), 2);
        mvc.perform(patch("/api/requests/" + rid + "/approve")
                        .header("Authorization", "Bearer " + ana.token()))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/requests").header("Authorization", "Bearer " + ana.token()))
                .andExpect(status().isForbidden());
    }

    // ---------------- validaciones ----------------
    @Test
    @DisplayName("Código de producto duplicado: 409. Precio negativo: 400 con errores por campo")
    void duplicateAndValidation() throws Exception {
        String admin = adminToken();
        String json = "{\"code\":\"DUP-1\",\"name\":\"X\",\"category\":\"C\",\"price\":5,\"stock\":1}";
        // el primero puede existir de otra ejecución del mismo contenedor; solo importa el segundo
        mvc.perform(post("/api/products").header("Authorization", "Bearer " + admin).contentType(JSON).content(json));
        mvc.perform(post("/api/products").header("Authorization", "Bearer " + admin).contentType(JSON).content(json))
                .andExpect(status().isConflict());

        mvc.perform(post("/api/products").header("Authorization", "Bearer " + admin).contentType(JSON)
                        .content("{\"code\":\"\",\"name\":\"X\",\"category\":\"C\",\"price\":-5,\"stock\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.price").exists());
    }

    // ---------------- flujo completo ----------------
    @Test
    @DisplayName("Flujo: crear -> propiedad -> aprobar -> entregar descuenta stock y registra movimiento")
    void fullFlow() throws Exception {
        Person ana = employee();
        Person luis = employee();
        Person beto = warehouse();
        long pid = product(10);
        long rid = request(ana, pid, 5);

        // propiedad: otro empleado recibe 404, no 403
        mvc.perform(get("/api/requests/" + rid).header("Authorization", "Bearer " + luis.token()))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/requests/" + rid).header("Authorization", "Bearer " + ana.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDIENTE"));

        // no se puede entregar sin aprobar
        mvc.perform(patch("/api/requests/" + rid + "/deliver").header("Authorization", "Bearer " + beto.token()))
                .andExpect(status().isConflict());

        patchAs(beto, "/api/requests/" + rid + "/approve");
        patchAs(beto, "/api/requests/" + rid + "/deliver");

        // no se puede entregar dos veces
        mvc.perform(patch("/api/requests/" + rid + "/deliver").header("Authorization", "Bearer " + beto.token()))
                .andExpect(status().isConflict());

        mvc.perform(get("/api/products/" + pid).header("Authorization", "Bearer " + ana.token()))
                .andExpect(jsonPath("$.stock").value(5));
        mvc.perform(get("/api/movements").param("productId", String.valueOf(pid))
                        .header("Authorization", "Bearer " + beto.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].type").value("SALIDA"))
                .andExpect(jsonPath("$.content[0].quantity").value(5));
    }

    // ---------------- proveedor ----------------
    @Test
    @DisplayName("Sin stock local y proveedor con stock: 201 con nota en la observación")
    void supplierHasStock() throws Exception {
        when(supplier.check(any(), anyInt(), any()))
                .thenReturn(new SupplierAvailabilityPort.SupplierAvailability("SUP-002", true, 32, 1));
        Person ana = employee();
        long pid = product(1);

        mvc.perform(post("/api/requests").header("Authorization", "Bearer " + ana.token()).contentType(JSON)
                        .content("{\"productId\":" + pid + ",\"quantity\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.observation").value(org.hamcrest.Matchers.containsString("SUP-002")));
    }

    @Test
    @DisplayName("Proveedor caído: 503 y no se crea la solicitud")
    void supplierDown() throws Exception {
        when(supplier.check(any(), anyInt(), any()))
                .thenThrow(new SupplierUnavailableException("caído", null));
        Person ana = employee();
        long pid = product(1);

        mvc.perform(post("/api/requests").header("Authorization", "Bearer " + ana.token()).contentType(JSON)
                        .content("{\"productId\":" + pid + ",\"quantity\":2}"))
                .andExpect(status().isServiceUnavailable());

        mvc.perform(get("/api/requests/mine").header("Authorization", "Bearer " + ana.token()))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ---------------- transacción ----------------
    @Test
    @DisplayName("ROLLBACK: si falla el registro del movimiento, el stock y el estado no cambian")
    void deliveryRollsBackWhenMovementFails() throws Exception {
        Person ana = employee();
        Person beto = warehouse();
        long pid = product(10);
        long rid = request(ana, pid, 5);
        patchAs(beto, "/api/requests/" + rid + "/approve");

        // el INSERT del movimiento falla DESPUÉS de descontar el stock y cambiar el estado
        doThrow(new IllegalStateException("fallo simulado")).when(movements).save(any());

        mvc.perform(patch("/api/requests/" + rid + "/deliver").header("Authorization", "Bearer " + beto.token()))
                .andExpect(status().isInternalServerError());

        mvc.perform(get("/api/products/" + pid).header("Authorization", "Bearer " + beto.token()))
                .andExpect(jsonPath("$.stock").value(10));               // el descuento se revirtió
        mvc.perform(get("/api/requests/" + rid).header("Authorization", "Bearer " + beto.token()))
                .andExpect(jsonPath("$.status").value("APROBADA"));      // sigue aprobada, no entregada
        mvc.perform(get("/api/movements").param("productId", String.valueOf(pid))
                        .header("Authorization", "Bearer " + beto.token()))
                .andExpect(jsonPath("$.totalElements").value(0));        // sin movimiento huérfano
    }
}
