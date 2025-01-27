package com.hubbox.demo.dependecy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hubbox.demo.config.CacheConfig;
import com.hubbox.demo.config.CacheManager;
import com.hubbox.demo.config.ConfigurationLoaderManager;
import com.hubbox.demo.config.ConfigurationManager;
import com.hubbox.demo.config.MqttConfig;
import com.hubbox.demo.controller.DeviceCategoryController;
import com.hubbox.demo.controller.DeviceCommandController;
import com.hubbox.demo.controller.DeviceController;
import com.hubbox.demo.entities.DeviceCommandEntity;
import com.hubbox.demo.mapper.DeviceCategoryMapper;
import com.hubbox.demo.mapper.DeviceCommandMapper;
import com.hubbox.demo.repository.DeviceCategoryRepository;
import com.hubbox.demo.repository.DeviceCommandRepository;
import com.hubbox.demo.service.DeviceCategoryService;
import com.hubbox.demo.service.DeviceCommandService;
import com.hubbox.demo.service.DeviceService;
import com.hubbox.demo.service.MqttService;
import com.hubbox.demo.util.CacheNames;
import java.util.List;

public class AppModule extends AbstractModule {
    @Override
    protected void configure() {

        // Base configurations
        bind(ObjectMapper.class).toInstance(ObjectMapperFactory.create());
        bind(ConfigurationLoaderManager.class).in(Singleton.class);
        bind(CacheManager.class).in(Singleton.class);

        // Repositories
        bind(DeviceCategoryRepository.class).in(Singleton.class);
        bind(DeviceCommandRepository.class).in(Singleton.class);

        // Mappers
        bind(DeviceCategoryMapper.class).toInstance(DeviceCategoryMapper.INSTANCE);
        bind(DeviceCommandMapper.class).toInstance(DeviceCommandMapper.INSTANCE);

        // Services
        bind(MqttService.class).in(Singleton.class);
        bind(DeviceService.class).in(Singleton.class);
        bind(DeviceCategoryService.class).in(Singleton.class);
        bind(DeviceCommandService.class).in(Singleton.class);

        // Controller
        bind(DeviceController.class).in(Singleton.class);
        bind(DeviceCategoryController.class).in(Singleton.class);
        bind(DeviceCommandController.class).in(Singleton.class);

        bindConstant().annotatedWith(Names.named("serverPort")).to(8080);
    }

    @Provides
    @Singleton
    Cache<String, List<DeviceCommandEntity>> provideDeviceCommandCache(CacheManager cacheManager) {
        return cacheManager.getCache(CacheNames.DEVICE_COMMANDS);
    }

    @Provides
    @Singleton
    MqttConfig provideMqttConfig(ConfigurationLoaderManager configManager) {
        return configManager.getAppConfig().mqtt();
    }

    @Provides
    @Singleton
    Mqtt3AsyncClient provideMqttClient(MqttConfig mqttConfig) {
        return Mqtt3Client.builder()
            .identifier(mqttConfig.id())
            .serverHost(mqttConfig.host())
            .serverPort(mqttConfig.port())
            .buildAsync();
    }

    @Provides
    @Singleton
    ConfigurationManager provideConfigurationManager() {
        return ConfigurationManager.getInstance();
    }

    @Provides
    @Singleton
    CacheConfig provideCacheConfig(ConfigurationLoaderManager configManager) {
        return configManager.getAppConfig().cache();
    }
}