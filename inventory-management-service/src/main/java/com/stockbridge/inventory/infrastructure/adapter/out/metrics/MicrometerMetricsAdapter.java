package com.stockbridge.inventory.infrastructure.adapter.out.metrics;

import com.stockbridge.inventory.application.port.out.MetricsPort;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class MicrometerMetricsAdapter implements MetricsPort {
    private final MeterRegistry registry;
    public MicrometerMetricsAdapter(MeterRegistry registry) { this.registry = registry; }

    @Override
    public void count(String name) { registry.counter(name).increment(); }
}
