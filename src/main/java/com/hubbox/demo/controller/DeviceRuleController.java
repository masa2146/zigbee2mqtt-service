package com.hubbox.demo.controller;

import static com.hubbox.demo.util.Constants.CONTEXT_PATH;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubbox.demo.dto.request.DeviceRuleCreateRequest;
import com.hubbox.demo.dto.request.DeviceRuleUpdateRequest;
import com.hubbox.demo.dto.response.DeviceRuleResponse;
import com.hubbox.demo.service.DeviceRuleService;
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
public class DeviceRuleController extends AbstractController {
    private final DeviceRuleService ruleService;

    @Inject
    public DeviceRuleController(DeviceRuleService ruleService) {
        super("rules");
        this.ruleService = ruleService;
    }

    @Override
    public void registerRoutes(Javalin app) {
        app.post(buildPath(), this::createRule);
        app.get(buildPath(), this::getAllRules);
        app.get(buildPath("{id}"), this::getRule);
        app.put(buildPath("{id}"), this::updateRule);
        app.delete(buildPath("{id}"), this::deleteRule);
    }

    @OpenApi(
        path = CONTEXT_PATH + "/rules",
        methods = {HttpMethod.POST},
        summary = "Create a new device rule",
        operationId = "createRule",
        tags = {"Device Rules"},
        requestBody = @OpenApiRequestBody(
            content = {@OpenApiContent(from = DeviceRuleCreateRequest.class)}
        ),
        responses = {
            @OpenApiResponse(status = "201", content = {@OpenApiContent(from = DeviceRuleResponse.class)}),
            @OpenApiResponse(status = "400", description = "Invalid request")
        }
    )
    private void createRule(Context ctx) {
        DeviceRuleCreateRequest request = ctx.bodyAsClass(DeviceRuleCreateRequest.class);
        DeviceRuleResponse response = ruleService.createRule(request);
        ctx.status(201).json(response);
    }

    @OpenApi(
        path = CONTEXT_PATH + "/rules",
        methods = {HttpMethod.GET},
        summary = "Get all device rules",
        operationId = "getAllRules",
        tags = {"Device Rules"},
        responses = {
            @OpenApiResponse(
                status = "200",
                content = {@OpenApiContent(from = DeviceRuleResponse[].class)}
            )
        }
    )
    private void getAllRules(Context ctx) {
        List<DeviceRuleResponse> responses = ruleService.getAllRules();
        ctx.json(responses);
    }

    @OpenApi(
        path = CONTEXT_PATH + "/rules/{id}",
        methods = {HttpMethod.GET},
        summary = "Get rule by ID",
        operationId = "getRule",
        tags = {"Device Rules"},
        pathParams = {
            @OpenApiParam(name = "id", type = Long.class, description = "Rule ID")
        },
        responses = {
            @OpenApiResponse(status = "200", content = {@OpenApiContent(from = DeviceRuleResponse.class)}),
            @OpenApiResponse(status = "404", description = "Rule not found")
        }
    )
    private void getRule(Context ctx) {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        DeviceRuleResponse response = ruleService.getRule(id);
        ctx.json(response);
    }

    @OpenApi(
        path = CONTEXT_PATH + "/rules/{id}",
        methods = {HttpMethod.PUT},
        summary = "Update an existing device rule",
        operationId = "updateRule",
        tags = {"Device Rules"},
        pathParams = {
            @OpenApiParam(name = "id", type = Long.class, description = "Rule ID")
        },
        requestBody = @OpenApiRequestBody(
            content = {@OpenApiContent(from = DeviceRuleUpdateRequest.class)}
        ),
        responses = {
            @OpenApiResponse(status = "200", content = {@OpenApiContent(from = DeviceRuleResponse.class)}),
            @OpenApiResponse(status = "400", description = "Invalid request"),
            @OpenApiResponse(status = "404", description = "Rule not found")
        }
    )
    private void updateRule(Context ctx) {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        DeviceRuleUpdateRequest request = ctx.bodyAsClass(DeviceRuleUpdateRequest.class);
        DeviceRuleResponse response = ruleService.updateRule(id, request);
        ctx.json(response);
    }

    @OpenApi(
        path = CONTEXT_PATH + "/rules/{id}",
        methods = {HttpMethod.DELETE},
        summary = "Delete a device rule",
        operationId = "deleteRule",
        tags = {"Device Rules"},
        pathParams = {
            @OpenApiParam(name = "id", type = Long.class, description = "Rule ID")
        },
        responses = {
            @OpenApiResponse(status = "204", description = "Rule deleted successfully"),
            @OpenApiResponse(status = "404", description = "Rule not found")
        }
    )
    private void deleteRule(Context ctx) {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        ruleService.deleteRule(id);
        ctx.status(204);
    }
}
