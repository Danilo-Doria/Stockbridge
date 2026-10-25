package com.stockbridge.inventory.infrastructure.adapter.out.supplier;

import com.stockbridge.inventory.application.port.out.SupplierAvailabilityPort;
import com.stockbridge.inventory.domain.exception.SupplierUnavailableException;
import com.stockbridge.inventory.infrastructure.config.SupplierProperties;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;

@Slf4j
@Component
public class SupplierHttpAdapter implements SupplierAvailabilityPort {

    private record Req(String productCode, int quantity, String category) {}
    private record Res(String supplierCode, boolean available,
                       int availableQuantity, int estimatedDeliveryDays) {}

    private final RestClient client;
    private final MeterRegistry registry;

    public SupplierHttpAdapter(SupplierProperties props, MeterRegistry registry) {
        this.registry = registry;
        HttpClient http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(props.connectTimeoutMs()))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(http);
        factory.setReadTimeout(Duration.ofMillis(props.readTimeoutMs()));
        this.client = RestClient.builder().baseUrl(props.baseUrl()).requestFactory(factory).build();
    }

    @Override
    public SupplierAvailability check(String productCode, int quantity, String category) {
        Timer.Sample sample = Timer.start(registry);
        try {
            Res r = client.post().uri("/api/suppliers/check-availability")
                    .body(new Req(productCode, quantity, category))
                    .retrieve()
                    .body(Res.class);
            if (r == null) {
                throw new SupplierUnavailableException("Respuesta vacía del proveedor", null);
            }
            sample.stop(registry.timer("stockbridge.supplier.latency", "outcome", "ok"));
            log.info("Proveedor consultado para {}", productCode);
            return new SupplierAvailability(r.supplierCode(), r.available(),
                    r.availableQuantity(), r.estimatedDeliveryDays());
        } catch (RestClientException ex) {   // timeout, conexión rechazada, 4xx o 5xx
            sample.stop(registry.timer("stockbridge.supplier.latency", "outcome", "error"));
            registry.counter("stockbridge.supplier.errors").increment();
            log.warn("Falla consultando proveedor para {}: {}", productCode, ex.getMessage());
            throw new SupplierUnavailableException("No se pudo consultar al proveedor", ex);
        }
    }
}