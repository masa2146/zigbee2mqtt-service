package com.hubbox.demo.dependecy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubbox.demo.config.CacheManager;
import com.hubbox.demo.config.ConfigurationLoaderManager;
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
import lombok.Getter;

@Getter
public class DependencyContainer {
    private final ObjectMapper objectMapper;
    private final ConfigurationLoaderManager configManager;
    private final CacheManager cacheManager;

    // Repositories
    private final DeviceCategoryRepository categoryRepository;
    private final DeviceCommandRepository commandRepository;
    private final DeviceModelRepository modelRepository;

    // Mappers
    private final DeviceCategoryMapper categoryMapper;
    private final DeviceCommandMapper commandMapper;
    private final DeviceModelMapper modelMapper;

    // Services
    private final MqttService mqttService;
    private final DeviceService deviceService;
    private final DeviceCategoryService categoryService;
    private final DeviceModelService modelService;
    private final DeviceCommandService commandService;

    private DependencyContainer(Builder builder) {
        this.objectMapper = builder.objectMapper;
        this.configManager = builder.configManager;
        this.cacheManager = builder.cacheManager;

        // Initialize repositories
        this.categoryRepository = builder.categoryRepository;
        this.commandRepository = builder.commandRepository;
        this.modelRepository = builder.modelRepository;

        // Initialize mappers
        this.categoryMapper = builder.categoryMapper;
        this.commandMapper = builder.commandMapper;
        this.modelMapper = builder.modelMapper;

        // Initialize services
        this.mqttService = builder.mqttService;
        this.deviceService = builder.deviceService;
        this.categoryService = builder.categoryService;
        this.modelService = builder.modelService;
        this.commandService = builder.commandService;
    }

    public static class Builder {
        private final ObjectMapper objectMapper;
        private final ConfigurationLoaderManager configManager;
        private final CacheManager cacheManager;

        // Repositories
        private final DeviceCategoryRepository categoryRepository;
        private final DeviceCommandRepository commandRepository;
        private final DeviceModelRepository modelRepository;

        // Mappers
        private final DeviceCategoryMapper categoryMapper;
        private final DeviceCommandMapper commandMapper;
        private final DeviceModelMapper modelMapper;

        // Services
        private final MqttService mqttService;
        private final DeviceService deviceService;
        private final DeviceCategoryService categoryService;
        private final DeviceModelService modelService;
        private final DeviceCommandService commandService;

        public Builder() {
            // Initialize base dependencies
            this.objectMapper = ObjectMapperFactory.create();
            this.configManager = ConfigurationLoaderManager.getInstance();
            this.cacheManager = CacheManager.getInstance();

            // Initialize repositories
            this.categoryRepository = RepositoryFactory.createCategoryRepository();
            this.commandRepository = RepositoryFactory.createCommandRepository();
            this.modelRepository = RepositoryFactory.createModelRepository();

            // Initialize mappers
            this.categoryMapper = DeviceCategoryMapper.INSTANCE;
            this.commandMapper = DeviceCommandMapper.INSTANCE;
            this.modelMapper = DeviceModelMapper.INSTANCE;

            // Initialize services
            ServiceFactory serviceFactory = new ServiceFactory(
                objectMapper, configManager, cacheManager);

            this.mqttService = serviceFactory.createMqttService();
            this.categoryService = serviceFactory.createCategoryService(categoryRepository, categoryMapper);
            this.modelService = serviceFactory.createModelService(modelRepository, modelMapper, categoryService);
            this.commandService = serviceFactory.createCommandService(commandRepository, commandMapper, modelService);
            this.deviceService = serviceFactory.createDeviceService(mqttService, commandService);

        }

        public DependencyContainer build() {
            return new DependencyContainer(this);
        }
    }
}

