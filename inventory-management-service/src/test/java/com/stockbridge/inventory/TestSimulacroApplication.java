package com.stockbridge.inventory;

import org.springframework.boot.SpringApplication;

public class TestSimulacroApplication {

    public static void main(String[] args) {
        SpringApplication.from(SimulacroApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
