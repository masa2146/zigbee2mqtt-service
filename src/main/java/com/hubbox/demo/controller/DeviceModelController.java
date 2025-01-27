package com.hubbox.demo.controller;

import static com.hubbox.demo.util.Constants.CONTEXT_PATH;

import com.hubbox.demo.dto.request.DeviceModelCreateRequest;
import com.hubbox.demo.dto.response.DeviceModelResponse;
import com.hubbox.demo.service.DeviceModelService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeviceModelController {
    private final DeviceModelService modelService;

    public DeviceModelController(DeviceModelService modelService) {
        this.modelService = modelService;
    }

    public void registerRoutes(Javalin app) {
        app.post("/models", this::createModel);
        app.get("/models/{modelId}", this::getModel);
    }

    @OpenApi(
        path = CONTEXT_PATH + "/models",
        methods = {HttpMethod.POST},
        summary = "Create a new device model",
        operationId = "createModel",
        tags = {"Device Models"},
        requestBody = @OpenApiRequestBody(
            content = {@OpenApiContent(from = DeviceModelCreateRequest.class)}
        ),
        responses = {
            @OpenApiResponse(status = "201", content = {@OpenApiContent(from = DeviceModelResponse.class)}),
            @OpenApiResponse(status = "400", description = "Invalid request"),
            @OpenApiResponse(status = "404", description = "Category not found")
        }
    )
    private void createModel(Context ctx) {
        DeviceModelCreateRequest request = ctx.bodyAsClass(DeviceModelCreateRequest.class);
        DeviceModelResponse response = modelService.createModel(request);
        ctx.status(201).json(response);
    }

    @OpenApi(
        path = CONTEXT_PATH + "/models/{modelId}",
        methods = {HttpMethod.GET},
        summary = "Get model by ID",
        operationId = "getModel",
        tags = {"Device Models"},
        pathParams = {
            @OpenApiParam(name = "modelId", description = "Model ID")
        },
        responses = {
            @OpenApiResponse(status = "200", content = {@OpenApiContent(from = DeviceModelResponse.class)}),
            @OpenApiResponse(status = "404", description = "Model not found")
        }
    )
    private void getModel(Context ctx) {
        String modelId = ctx.pathParam("modelId");
        DeviceModelResponse response = modelService.getModelByModelId(modelId);
        ctx.json(response);
    }
}
