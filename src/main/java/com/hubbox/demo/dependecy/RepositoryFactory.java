package com.hubbox.demo.dependecy;

import com.hubbox.demo.exceptions.DependencyInitializationException;
import com.hubbox.demo.repository.DeviceCategoryRepository;
import com.hubbox.demo.repository.DeviceCommandRepository;
import com.hubbox.demo.repository.DeviceModelRepository;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class RepositoryFactory {
    private static final String REPOSITORY_CREATION_FAILED = "Repository creation failed";

    public static DeviceCategoryRepository createCategoryRepository() {
        try {
            return new DeviceCategoryRepository();
        } catch (Exception e) {
            log.error("Failed to create DeviceCategoryRepository", e);
            throw new DependencyInitializationException(REPOSITORY_CREATION_FAILED, e);
        }
    }

    public static DeviceCommandRepository createCommandRepository() {
        try {
            return new DeviceCommandRepository();
        } catch (Exception e) {
            log.error("Failed to create DeviceCommandRepository", e);
            throw new DependencyInitializationException(REPOSITORY_CREATION_FAILED, e);
        }
    }

    public static DeviceModelRepository createModelRepository() {
        try {
            return new DeviceModelRepository();
        } catch (Exception e) {
            log.error("Failed to create DeviceModelRepository", e);
            throw new DependencyInitializationException(REPOSITORY_CREATION_FAILED, e);
        }
    }
}
