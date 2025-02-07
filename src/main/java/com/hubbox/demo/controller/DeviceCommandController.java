package com.hubbox.demo.controller;

import static com.hubbox.demo.util.Constants.CONTEXT_PATH;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubbox.demo.dto.request.DeviceCommandCreateRequest;
import com.hubbox.demo.dto.request.DeviceCommandUpdateRequest;
import com.hubbox.demo.dto.request.SendDeviceCommandRequest;
import com.hubbox.demo.dto.response.DeviceCommandResponse;
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
@Singleton
public class DeviceCommandController extends AbstractController {
    private final DeviceCommandService commandService;

    @Inject
    public DeviceCommandController(DeviceCommandService commandService) {
        super("commands");
        this.commandService = commandService;
    }

    @Override
    public void registerRoutes(Javalin app) {
        app.post(buildPath(), this::createCommand);
        app.get(buildPath(), this::getAllCommands);
        app.get(buildPath("{id}"), this::getCommand);
        app.get(buildPath("model/{modelId}"), this::getCommandsByModel);
        app.put(buildPath("{id}"), this::updateCommand);
        app.delete(buildPath("{id}"), this::deleteCommand);
        app.post(buildPath("send"), this::sendCommand);
    }

    @OpenApi(
        path = CONTEXT_PATH + "/commands",
        methods = {HttpMethod.POST},
        summary = "Create a new device command",
        operationId = "createCommand",
        tags = {"Device Commands"},
        requestBody = @OpenApiRequestBody(
            content = {@OpenApiContent(from = DeviceCommandCreateRequest.class)}
        ),
        responses = {
            @OpenApiResponse(status = "201", content = {@OpenApiContent(from = DeviceCommandResponse.class)}),
            @OpenApiResponse(status = "400", description = "Invalid request"),
            @OpenApiResponse(status = "404", description = "Model not found")
        }
    )
    private void createCommand(Context ctx) {
        DeviceCommandCreateRequest request = ctx.bodyAsClass(DeviceCommandCreateRequest.class);
        DeviceCommandResponse response = commandService.createCommand(request);
        ctx.status(201).json(response);
    }

    @OpenApi(
        path = CONTEXT_PATH + "/commands",
        methods = {HttpMethod.GET},
        summary = "Get all commands",
        operationId = "getAllCommands",
        tags = {"Device Commands"},
        responses = {
            @OpenApiResponse(
                status = "200",
                content = {@OpenApiContent(from = DeviceCommandResponse[].class)}
            )
        }
    )
    private void getAllCommands(Context ctx) {
        List<DeviceCommandResponse> responses = commandService.getAllCommands();
        ctx.json(responses);
    }

    @OpenApi(
        path = CONTEXT_PATH + "/commands/{id}",
        methods = {HttpMethod.GET},
        summary = "Get command by ID",
        operationId = "getCommand",
        tags = {"Device Commands"},
        pathParams = {
            @OpenApiParam(name = "id", type = Long.class, description = "Command ID")
        },
        responses = {
            @OpenApiResponse(status = "200", content = {@OpenApiContent(from = DeviceCommandResponse.class)}),
            @OpenApiResponse(status = "404", description = "Command not found")
        }
    )
    private void getCommand(Context ctx) {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        DeviceCommandResponse response = commandService.getCommand(id);
        ctx.json(response);
    }

    @OpenApi(
        path = CONTEXT_PATH + "/commands/model/{modelId}",
        methods = {HttpMethod.GET},
        summary = "Get commands by model ID",
        operationId = "getCommandsByModel",
        tags = {"Device Commands"},
        pathParams = {
            @OpenApiParam(name = "modelId", description = "Model ID of the device")
        },
        responses = {
            @OpenApiResponse(
                status = "200",
                content = {@OpenApiContent(from = DeviceCommandResponse[].class)}
            ),
            @OpenApiResponse(status = "404", description = "Model not found")
        }
    )
    private void getCommandsByModel(Context ctx) {
        String modelId = ctx.pathParam("modelId");
        List<DeviceCommandResponse> commands = commandService.getCommandsByModel(modelId);
        ctx.json(commands);
    }

    @OpenApi(
        path = CONTEXT_PATH + "/commands/{id}",
        methods = {HttpMethod.PUT},
        summary = "Update an existing device command",
        operationId = "updateCommand",
        tags = {"Device Commands"},
        pathParams = {
            @OpenApiParam(name = "id", type = Long.class, description = "Command ID")
        },
        requestBody = @OpenApiRequestBody(
            content = {@OpenApiContent(from = DeviceCommandUpdateRequest.class)}
        ),
        responses = {
            @OpenApiResponse(status = "200", content = {@OpenApiContent(from = DeviceCommandResponse.class)}),
            @OpenApiResponse(status = "400", description = "Invalid request"),
            @OpenApiResponse(status = "404", description = "Command not found")
        }
    )
    private void updateCommand(Context ctx) {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        DeviceCommandUpdateRequest request = ctx.bodyAsClass(DeviceCommandUpdateRequest.class);
        DeviceCommandResponse response = commandService.updateCommand(id, request);
        ctx.json(response);
    }

    @OpenApi(
        path = CONTEXT_PATH + "/commands/{id}",
        methods = {HttpMethod.DELETE},
        summary = "Delete a device command",
        operationId = "deleteCommand",
        tags = {"Device Commands"},
        pathParams = {
            @OpenApiParam(name = "id", type = Long.class, description = "Command ID")
        },
        responses = {
            @OpenApiResponse(status = "204", description = "Command deleted successfully"),
            @OpenApiResponse(status = "404", description = "Command not found")
        }
    )
    private void deleteCommand(Context ctx) {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        commandService.deleteCommand(id);
        ctx.status(204);
    }

    @OpenApi(
        path = CONTEXT_PATH + "/commands/send",
        methods = {HttpMethod.POST},
        summary = "Send command newName device",
        operationId = "sendCommand",
        tags = {"Device Commands"},
        requestBody = @OpenApiRequestBody(
            content = {@OpenApiContent(from = SendDeviceCommandRequest.class)}
        ),
        responses = {
            @OpenApiResponse(status = "200", description = "Command sent successfully"),
            @OpenApiResponse(status = "404", description = "Device not found"),
            @OpenApiResponse(status = "400", description = "Invalid command")
        }
    )
    private void sendCommand(Context ctx) {
        SendDeviceCommandRequest request = ctx.bodyAsClass(SendDeviceCommandRequest.class);

        commandService.executeCommand(request);

        ctx.status(200).result("Command sent successfully");
    }
}
