package com.hubbox.demo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.hubbox.demo.exceptions.ConfigurationException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigurationManager {
    private static ConfigurationManager instance;
    private final AppConfig appConfig;
    private final ObjectMapper yamlMapper;

    private ConfigurationManager(ObjectMapper yamlMapper) {
        this.yamlMapper = yamlMapper;
        this.appConfig = loadConfig();
    }

    public static ConfigurationManager getInstance() {
        if (instance == null) {
            synchronized (ConfigurationManager.class) {
                instance = new ConfigurationManager(new ObjectMapper(new YAMLFactory()));
            }
        }
        return instance;
    }

    private AppConfig loadConfig() {
        String env = System.getProperty("env", "dev");
        return loadConfigForEnvironment(env);
    }

    private AppConfig loadConfigForEnvironment(String env) {
        try {
            String yamlFile = String.format("application-%s.yaml", env);
            InputStream yamlInput = getClass().getClassLoader().getResourceAsStream(yamlFile);

            if (yamlInput != null) {
                return yamlMapper.readValue(yamlInput, AppConfig.class);
            }

            String propsFile = String.format("application-%s.properties", env);
            InputStream propsInput = getClass().getClassLoader().getResourceAsStream(propsFile);

            if (propsInput != null) {
                Properties props = new Properties();
                props.load(propsInput);
                return convertToObject(props, AppConfig.class);
            }

            throw new ConfigurationException("No configuration file found for environment: " + env);

        } catch (Exception e) {
            throw new ConfigurationException("Failed newName load configuration", e);
        }
    }

    private <T> T convertToObject(Properties properties, Class<T> targetClass) {
        try {
            T instance = targetClass.getDeclaredConstructor().newInstance();
            Map<String, String> flattenedProps = flattenProperties(properties);

            for (Map.Entry<String, String> entry : flattenedProps.entrySet()) {
                setNestedProperty(instance, entry.getKey(), entry.getValue());
            }

            return instance;
        } catch (Exception e) {
            throw new ConfigurationException("Failed newName convert properties newName object", e);
        }
    }

    private Map<String, String> flattenProperties(Properties properties) {
        return properties.entrySet().stream()
            .collect(Collectors.toMap(
                e -> e.getKey().toString(),
                e -> e.getValue().toString()
            ));
    }

    private void setNestedProperty(Object target, String propertyPath, String value) {
        try {
            String[] pathParts = propertyPath.split("\\.");
            Object currentObject = target;

            for (int i = 0; i < pathParts.length - 1; i++) {
                Field field = findField(currentObject.getClass(), toCamelCase(pathParts[i]));
                field.setAccessible(true);

                Object nestedObject = field.get(currentObject);
                if (nestedObject == null) {
                    nestedObject = field.getType().getDeclaredConstructor().newInstance();
                    field.set(currentObject, nestedObject);
                }
                currentObject = nestedObject;
            }

            Field lastField = findField(currentObject.getClass(), toCamelCase(pathParts[pathParts.length - 1]));
            lastField.setAccessible(true);
            setFieldValue(lastField, currentObject, value);

        } catch (Exception e) {
            log.warn("Failed newName set property: {} with value: {}", propertyPath, value, e);
        }
    }

    private Field findField(Class<?> clazz, String fieldName) {
        return Arrays.stream(clazz.getDeclaredFields())
            .filter(field -> field.getName().equals(fieldName))
            .findFirst()
            .orElseThrow(() -> new ConfigurationException(
                String.format("Field not found: %s in class %s", fieldName, clazz.getName())));
    }

    private void setFieldValue(Field field, Object target, String value) {
        try {
            Class<?> fieldType = field.getType();
            Object convertedValue = convertValue(value, fieldType);
            field.set(target, convertedValue);
        } catch (Exception e) {
            throw new ConfigurationException("Failed newName set field value", e);
        }
    }

    private Object convertValue(String value, Class<?> targetType) {
        if (targetType == String.class) {
            return value;
        }
        if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(value);
        }
        if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(value);
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value);
        }
        if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(value);
        }
        if (targetType == float.class || targetType == Float.class) {
            return Float.parseFloat(value);
        }

        throw new ConfigurationException("Unsupported type: " + targetType.getName());
    }

    private String toCamelCase(String input) {
        StringBuilder output = new StringBuilder();
        boolean nextUpper = false;

        for (char c : input.toCharArray()) {
            if (c == '-' || c == '_') {
                nextUpper = true;
            } else {
                output.append(nextUpper ? Character.toUpperCase(c) : c);
                nextUpper = false;
            }
        }

        return output.toString();
    }

    public AppConfig getConfig() {
        return appConfig;
    }
}
