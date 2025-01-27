package com.hubbox.demo.config;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;

@Singleton
@Getter
public class ConfigurationLoaderManager {

    private final AppConfig appConfig;

    @Inject
    public ConfigurationLoaderManager(ConfigurationManager configManager) {
        this.appConfig = configManager.getConfig();
    }
}
