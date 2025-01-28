package com.hubbox.demo.config;

import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class ShutdownManager {
    private final List<AutoCloseable> managedServices = new ArrayList<>();

    public void registerService(AutoCloseable service) {
        managedServices.add(service);
    }

    public void initShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Starting graceful shutdown...");
            shutdownAll();
            log.info("Graceful shutdown completed");
        }));
    }

    private void shutdownAll() {
        for (int i = managedServices.size() - 1; i >= 0; i--) {
            AutoCloseable service = managedServices.get(i);
            try {
                service.close();
                log.info("Successfully shut down: {}", service.getClass().getSimpleName());
            } catch (Exception e) {
                log.error("Error shutting down {}: {}",
                    service.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
    }
}
