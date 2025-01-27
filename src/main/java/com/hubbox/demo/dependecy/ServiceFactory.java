package com.hubbox.demo.dependecy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.hubbox.demo.config.CacheManager;
import com.hubbox.demo.config.ConfigurationLoaderManager;
import com.hubbox.demo.entities.DeviceCommand;
import com.hubbox.demo.mapper.DeviceCategoryMapper;
import com.hubbox.demo.mapper.DeviceCommandMapper;
import com.hubbox.demo.mapper.DeviceModelMapper;
import com.hubbox.demo.repository.DeviceCategoryRepository;
import com.hubbox.demo.repository.DeviceCommandRepository;
import com.hubbox.demo.repository.DeviceModelRepository;
import com.hubbox.demo.service.DeviceCategoryService;
import com.hubbox.demo.service.DeviceCommandService;
import com.hubbox.demo.service.DeviceModelService;
import com.hubbox.demo.service.DeviceService;
import com.hubbox.demo.service.MqttService;
import com.hubbox.demo.util.CacheNames;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServiceFactory {
    private final ObjectMapper objectMapper;
    private final ConfigurationLoaderManager configManager;
    private final CacheManager cacheManager;

    public ServiceFactory(ObjectMapper objectMapper,
                          ConfigurationLoaderManager configManager,
                          CacheManager cacheManager) {
        this.objectMapper = objectMapper;
        this.configManager = configManager;
        this.cacheManager = cacheManager;
    }

    public MqttService createMqttService() {
        return new MqttService(
            configManager.getMqttConfiguration().mqttClient(),
            configManager.getAppConfig().mqtt()
        );
    }

    public DeviceService createDeviceService(MqttService mqttService, DeviceCommandService commandService) {
        return new DeviceService(mqttService, objectMapper, commandService);
    }

    public DeviceCategoryService createCategoryService(
        DeviceCategoryRepository repository,
        DeviceCategoryMapper mapper) {
        return new DeviceCategoryService(repository, mapper);
    }

    public DeviceModelService createModelService(
        DeviceModelRepository repository,
        DeviceModelMapper mapper,
        DeviceCategoryService categoryService) {
        return new DeviceModelService(repository, mapper, categoryService);
    }

    public DeviceCommandService createCommandService(
        DeviceCommandRepository repository,
        DeviceCommandMapper mapper,
        DeviceModelService modelService) {
        Cache<String, List<DeviceCommand>> cache = cacheManager.getCache(CacheNames.DEVICE_COMMANDS);
        return new DeviceCommandService(repository, mapper, modelService, cache);
    }
}
