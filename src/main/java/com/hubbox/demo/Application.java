package com.hubbox.demo;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubbox.demo.config.SchemaInitializer;
import com.hubbox.demo.config.ShutdownManager;
import com.hubbox.demo.service.DeviceRuleService;
import com.hubbox.demo.service.DeviceService;
import com.hubbox.demo.service.MqttService;
import com.hubbox.demo.service.SensorEventManager;

@Singleton
public class Application {
    private final Server server;
    private final ShutdownManager shutdownManager;
    private final SchemaInitializer schemaInitializer;
    private final DeviceService deviceService;
    private final SensorEventManager eventManager;
    private final MqttService mqttService;

    @Inject
    public Application(Server server,
                       ShutdownManager shutdownManager,
                       SchemaInitializer schemaInitializer,
                       DeviceService deviceService,
                       SensorEventManager eventManager,
                       MqttService mqttService,
                       DeviceRuleService deviceRuleService) {
        this.server = server;
        this.shutdownManager = shutdownManager;
        this.schemaInitializer = schemaInitializer;
        this.deviceService = deviceService;
        this.eventManager = eventManager;
        this.mqttService = mqttService;
        this.eventManager.addListener(deviceRuleService);
    }

    public void start() {
        schemaInitializer.initializeSchema();
        shutdownManager.registerService(deviceService);
        shutdownManager.registerService(eventManager);
        shutdownManager.registerService(mqttService);
        server.start();
        shutdownManager.initShutdownHook();
    }
}
