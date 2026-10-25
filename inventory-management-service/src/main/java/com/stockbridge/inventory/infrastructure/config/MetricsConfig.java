package com.stockbridge.inventory.infrastructure.config;

import com.stockbridge.inventory.infrastructure.adapter.out.persistence.SpringDataProductRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    MeterBinder lowStockGauge(SpringDataProductRepository repo) {
        return registry -> Gauge.builder("stockbridge.products.low_stock",
                        () -> repo.countByStockLessThan(5))
                .description("Productos con stock menor a 5")
                .register(registry);
    }
}
