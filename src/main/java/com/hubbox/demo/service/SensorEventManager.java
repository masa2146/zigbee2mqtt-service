package com.hubbox.demo.service;

import com.google.inject.Singleton;
import com.hubbox.demo.listener.SensorEventListener;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class SensorEventManager implements AutoCloseable {
    private final ExecutorService executorService;
    private final List<SensorEventListener> eventListeners = new CopyOnWriteArrayList<>();
    private static final int QUEUE_SIZE = 1000;

    public SensorEventManager() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            2,
            4,
            60L,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(QUEUE_SIZE),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );

        executor.allowCoreThreadTimeOut(true);
        this.executorService = executor;
    }

    public void addListener(SensorEventListener listener) {
        eventListeners.add(listener);
    }

    public void publishEvent(String deviceName, Map<String, Object> data) {
        if (eventListeners.isEmpty()) {
            return;
        }

        executorService.execute(() -> {
            for (SensorEventListener listener : eventListeners) {
                try {
                    listener.onDeviceDataReceived(deviceName, data);
                } catch (Exception e) {
                    log.error("Error processing event for device: {}", deviceName, e);
                }
            }
        });
    }

    @Override
    public void close() throws Exception {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
