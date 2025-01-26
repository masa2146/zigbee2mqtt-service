package com.hubbox.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubbox.demo.controller.DeviceController;
import com.hubbox.demo.dependecy.DependencyManager;
import com.hubbox.demo.service.DeviceCommandService;
import io.javalin.Javalin;
import io.javalin.openapi.OpenApiServer;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.OpenApiPluginConfiguration;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Server {
    private final Javalin app;
    private final int port;
    private final DependencyManager dependencyManager;

    public Server(int port) {
        this.port = port;
        this.dependencyManager = DependencyManager.getInstance();
        this.app = Javalin.create(config -> {
            config.registerPlugin(configureOpenApi());
            config.registerPlugin(new SwaggerPlugin(swaggerConfig -> {
                swaggerConfig.setDocumentationPath("/openapi-docs"); // OpenAPI JSON/YAML
                swaggerConfig.setUiPath("/swagger-ui");             // Swagger UI
            }));
            config.bundledPlugins.enableCors(cors -> cors.addRule(it -> {
                it.anyHost();
                it.exposeHeader("Content-Type");
            }));
            config.http.defaultContentType = "application/json";
        });

        configureDependencies();
        configureRoutes();
    }

    private OpenApiPlugin configureOpenApi() {
        return new OpenApiPlugin(config -> {
            config.withDefinitionConfiguration((version, definition) -> {
                definition.withInfo(openApi -> {
                    openApi.title("Device API");
                    openApi.version("1.0");
                    openApi.description("API for managing devices");
                });
            });
            config.withDocumentationPath("/openapi-docs");
        });
    }

    private void configureDependencies() {
        // Dependency setup
        DeviceCommandService commandService = dependencyManager.getContainer().getCommandService();
        ObjectMapper objectMapper = dependencyManager.getContainer().getObjectMapper();
        DeviceController deviceController = new DeviceController(commandService, objectMapper);

        // Register routes
        deviceController.registerRoutes(app);
    }

    private void configureRoutes() {
        // Health check
        app.get("/health", ctx -> ctx.result("OK"));

        // Add more global routes here
    }

    public void start() {
        app.start(port);
        log.info("Server started on port {}", port);
    }

    public void stop() {
        app.stop();
        log.info("Server stopped");
    }
}
