package com.hubbox.demo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubbox.demo.dto.request.DeviceCommandCreateRequest;
import com.hubbox.demo.dto.request.SendCommandRequest;
import com.hubbox.demo.dto.response.DeviceCommandResponse;
import com.hubbox.demo.dto.response.ErrorResponse;
import com.hubbox.demo.service.DeviceCommandService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeviceController {
    private final DeviceCommandService commandService;
    private final ObjectMapper objectMapper;

    public DeviceController(DeviceCommandService commandService, ObjectMapper objectMapper) {
        this.commandService = commandService;
        this.objectMapper = objectMapper;
    }

    public void registerRoutes(Javalin app) {
        app.get("/api/devices/{modelId}/commands", this::getCommandsByModel);
        app.post("/api/devices/commands", this::createCommand);
        app.post("/api/devices/{deviceId}/send-command", this::sendCommand);
        app.exception(Exception.class, this::handleException);
    }

    @OpenApi(
        path = "/api/devices/{modelId}/commands",
        methods = HttpMethod.GET,
        summary = "Get commands by model ID",
        operationId = "getCommandsByModel",
        tags = {"Device Commands"},
        pathParams = {
            @OpenApiParam(
                name = "modelId",
                description = "The model ID of the device",
                required = true,
                type = String.class
            )
        },
        responses = {
            @OpenApiResponse(
                status = "200",
                description = "List of commands for the device model",
                content = {@OpenApiContent(from = DeviceCommandResponse[].class)}
            ),
            @OpenApiResponse(
                status = "404",
                description = "Model not found"
            )
        }
    )
    private void getCommandsByModel(Context ctx) {
        String modelId = ctx.pathParam("modelId");
        List<DeviceCommandResponse> commands = commandService.getCommandsByModel(modelId);
        ctx.json(commands);
    }

    @OpenApi(
        path = "/api/devices/commands",
        methods = HttpMethod.POST,
        summary = "Create a new device command",
        operationId = "createCommand",
        tags = {"Device Commands"},
        requestBody = @OpenApiRequestBody(
            content = {@OpenApiContent(from = DeviceCommandCreateRequest.class)}
        ),
        responses = {
            @OpenApiResponse(
                status = "201",
                description = "Command created successfully",
                content = {@OpenApiContent(from = DeviceCommandResponse.class)}
            ),
            @OpenApiResponse(
                status = "400",
                description = "Invalid request"
            )
        }
    )
    private void createCommand(Context ctx) throws JsonProcessingException {
        DeviceCommandCreateRequest request = objectMapper.readValue(
            ctx.body(), DeviceCommandCreateRequest.class);
        DeviceCommandResponse response = commandService.createCommand(request);
        ctx.status(201).json(response);
    }

    @OpenApi(
        path = "/api/devices/{deviceId}/send-command",
        methods = HttpMethod.POST,
        summary = "Send command to device",
        operationId = "sendCommand",
        tags = {"Device Commands"},
        pathParams = {
            @OpenApiParam(
                name = "deviceId",
                description = "The ID of the target device",
                required = true,
                type = String.class
            )
        },
        requestBody = @OpenApiRequestBody(
            content = {@OpenApiContent(from = SendCommandRequest.class)}
        ),
        responses = {
            @OpenApiResponse(
                status = "200",
                description = "Command sent successfully"
            ),
            @OpenApiResponse(
                status = "404",
                description = "Device not found"
            ),
            @OpenApiResponse(
                status = "400",
                description = "Invalid command"
            )
        }
    )
    private void sendCommand(Context ctx) throws JsonProcessingException {
        String deviceId = ctx.pathParam("deviceId");
        SendCommandRequest request = objectMapper.readValue(ctx.body(), SendCommandRequest.class);
        String commandTemplate = commandService.getCommandTemplate(request.modelId(), request.command());
        ctx.status(200).result("Command sent successfully");
    }

    private void handleException(Exception e, Context ctx) {
        log.error("Error handling request", e);
        ErrorResponse error = new ErrorResponse(500, "Internal Server Error", e.getMessage());
        ctx.status(500).json(error);
    }
}
