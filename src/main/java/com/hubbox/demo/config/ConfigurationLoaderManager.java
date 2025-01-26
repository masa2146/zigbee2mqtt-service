package com.hubbox.demo.config;

import lombok.Getter;

public class ConfigurationLoaderManager {
    private static ConfigurationLoaderManager configurationLoaderManager;

    @Getter
    private final MqttConfiguration mqttConfiguration;

    @Getter
    private final AppConfig appConfig;

    private ConfigurationLoaderManager() {
        ConfigurationManager instance = ConfigurationManager.getInstance();
        appConfig = instance.getConfig();
        mqttConfiguration = new MqttConfiguration(instance.getConfig().mqtt());
    }

    public static ConfigurationLoaderManager getInstance() {
        if (configurationLoaderManager == null) {
            configurationLoaderManager = new ConfigurationLoaderManager();
        }
        return configurationLoaderManager;
    }
}
