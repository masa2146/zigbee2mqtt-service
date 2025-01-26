package com.hubbox.demo.dependecy;


import com.hubbox.demo.exceptions.DependencyInitializationException;
import com.hubbox.demo.exceptions.DependencyShutdownException;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DependencyManager {
    private static AtomicReference<DependencyManager> instance;
    private static final Object LOCK = new Object();

    private final DependencyContainer container;

    private DependencyManager() {
        try {
            this.container = new DependencyContainer.Builder().build();
            log.info("DependencyManager initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize DependencyManager", e);
            throw new DependencyInitializationException("Failed to initialize dependencies", e);
        }
    }

    public static DependencyManager getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                instance = new AtomicReference<>(new DependencyManager());
            }
        }
        return instance.get();
    }

    public DependencyContainer getContainer() {
        return container;
    }

    public void shutdown() {
        try {
            log.info("Starting graceful shutdown...");
            container.getDeviceService().shutdown();
            container.getMqttService().disconnect();
            log.info("All dependencies shut down successfully");
        } catch (Exception e) {
            log.error("Error during shutdown", e);
            throw new DependencyShutdownException("Failed to shutdown", e);
        }
    }
}


