package com.hubbox.demo.controller;

import static com.hubbox.demo.util.Constants.CONTEXT_PATH;

import com.hubbox.demo.dto.request.DeviceCommandCreateRequest;
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
public class DeviceCommandController extends AbstractController {
    private final DeviceCommandService commandService;

    public DeviceCommandController(DeviceCommandService commandService) {
        super("commands");
        this.commandService = commandService;
    }

    public void registerRoutes(Javalin app) {
        app.post(buildPath(), this::createCommand);
        app.get(buildPath("{modelId}"), this::getCommandsByModel);
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
        path = CONTEXT_PATH + "/commands/{modelId}",
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
}
