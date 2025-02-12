package com.hubbox.demo;

import static com.hubbox.demo.util.Constants.CONTEXT_PATH;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.hubbox.demo.controller.DeviceCommandController;
import com.hubbox.demo.controller.DeviceController;
import com.hubbox.demo.controller.DeviceRuleController;
import com.hubbox.demo.controller.PinController;
import com.hubbox.demo.dto.response.ResponseMessage;
import com.hubbox.demo.exceptions.BaseRuntimeException;
import com.hubbox.demo.exceptions.RecordNotFoundException;
import io.javalin.Javalin;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class Server implements AutoCloseable {
    private final Javalin app;
    private final int port;
    private final DeviceCommandController commandController;
    private final DeviceController deviceController;
    private final DeviceRuleController deviceRuleController;
    private final PinController  pinController;

    @Inject
    public Server(
        @Named("serverPort") int port,
        DeviceCommandController commandController,
        DeviceController deviceController, DeviceRuleController deviceRuleController, PinController pinController) {
        this.port = port;
        this.commandController = commandController;
        this.deviceController = deviceController;
        this.deviceRuleController = deviceRuleController;
        this.pinController = pinController;

        this.app = configureJavalin();
        configureRoutes();
        configureExceptionHandling();
    }

    private Javalin configureJavalin() {
        return Javalin.create(config -> {
            config.registerPlugin(configureOpenApi());
            config.registerPlugin(new SwaggerPlugin(swaggerConfig -> {
                swaggerConfig.setDocumentationPath("/openapi-docs");
                swaggerConfig.setUiPath("/swagger-ui");
            }));

            config.router.contextPath = CONTEXT_PATH;
            config.bundledPlugins.enableCors(cors -> cors.addRule(it -> {
                it.anyHost();
                it.exposeHeader("Content-Type");
            }));
            config.http.defaultContentType = "application/json";
            config.bundledPlugins.enableDevLogging();
        });
    }

    private OpenApiPlugin configureOpenApi() {
        return new OpenApiPlugin(config -> {
            config.withDefinitionConfiguration((version, definition) -> definition.withInfo(openApi -> {
                openApi.title("Device Management API");
                openApi.version("1.0");
                openApi.description("API for managing IoT devices and commands");
            }));
            config.withDocumentationPath("/openapi-docs");
        });
    }

    private void configureRoutes() {
        commandController.registerRoutes(app);
        deviceController.registerRoutes(app);
        deviceRuleController.registerRoutes(app);
        pinController.registerRoutes(app);
    }

    private void configureExceptionHandling() {
        app.exception(RecordNotFoundException.class, (e, ctx) -> {
            log.error("Record not found", e);
            ctx.status(404).json(new ResponseMessage(404, "Not Found", e.getMessage()));
        });

        app.exception(BaseRuntimeException.class, (e, ctx) -> {
            log.error("Runtime error", e);
            ctx.status(400).json(new ResponseMessage(400, "Server Error", e.getMessage()));
        });

        app.exception(Exception.class, (e, ctx) -> {
            log.error("Unexpected error", e);
            ctx.status(500).json(new ResponseMessage(500, "Internal Server Error", "An unexpected error occurred"));
        });
    }

    public void start() {
        app.start(port);
        log.info("Server started on port {}", port);
        log.info("Swagger UI: http://localhost:{}/api/v1/swagger-ui", port);
        log.info("OpenAPI docs: http://localhost:{}/api/v1/openapi-docs", port);
    }

    @Override
    public void close() throws Exception {
        app.stop();
        log.info("Server stopped");
    }
}
