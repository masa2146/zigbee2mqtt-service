package com.hubbox.demo.controller;

import static com.hubbox.demo.util.Constants.CONTEXT_PATH;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubbox.demo.dto.request.PinCreateRequest;
import com.hubbox.demo.dto.request.PinUpdateRequest;
import com.hubbox.demo.dto.response.PinResponse;
import com.hubbox.demo.service.PinService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;
import java.sql.SQLException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class PinController extends AbstractController {
    private static final String PIN_PARAM = "pinNumber";
    private final PinService pinService;

    @Inject
    public PinController(PinService pinService) {
        super("pins");
        this.pinService = pinService;
    }

    @Override
    public void registerRoutes(Javalin app) {
        app.post(buildPath(), this::createPin);
        app.get(buildPath(), this::getAllPins);
        app.get(buildPath("{" + PIN_PARAM + "}"), this::getPin);
        app.put(buildPath("{" + PIN_PARAM + "}"), this::updatePin);
        app.delete(buildPath("{" + PIN_PARAM + "}"), this::deletePin);
    }

    @OpenApi(
        path = CONTEXT_PATH + "/pins",
        methods = {HttpMethod.POST},
        summary = "Create a new pin",
        operationId = "createPin",
        tags = {"Pins"},
        requestBody = @OpenApiRequestBody(
            content = {@OpenApiContent(from = PinCreateRequest.class)}
        ),
        responses = {
            @OpenApiResponse(status = "201", content = {@OpenApiContent(from = PinResponse.class)}),
            @OpenApiResponse(status = "400", description = "Invalid request")
        }
    )
    private void createPin(Context ctx) {
        PinCreateRequest request = ctx.bodyAsClass(PinCreateRequest.class);
        PinResponse response = pinService.createPin(request);
        ctx.status(201).json(response);
    }


    @OpenApi(
        path = CONTEXT_PATH + "/pins/{pinNumber}",
        methods = {HttpMethod.GET},
        summary = "Get a pin by pin number",
        operationId = "getPin",
        tags = {"Pins"},
        responses = {
            @OpenApiResponse(status = "200", content = {@OpenApiContent(from = PinResponse.class)}),
            @OpenApiResponse(status = "404", description = "Pin not found")
        }
    )
    private void getPin(Context ctx) {
        String pinNumber = ctx.pathParam(PIN_PARAM);
        PinResponse response = pinService.getPin(pinNumber);
        ctx.json(response);
    }

    @OpenApi(
        path = CONTEXT_PATH + "/pins/{pinNumber}",
        methods = {HttpMethod.PUT},
        summary = "Update a pin by pin number",
        operationId = "updatePin",
        tags = {"Pins"},
        requestBody = @OpenApiRequestBody(
            content = {@OpenApiContent(from = PinUpdateRequest.class)}
        ),
        responses = {
            @OpenApiResponse(status = "200", content = {@OpenApiContent(from = PinResponse.class)}),
            @OpenApiResponse(status = "400", description = "Invalid request"),
            @OpenApiResponse(status = "404", description = "Pin not found")
        }
    )
    private void updatePin(Context ctx) {
        String pinNumber = ctx.pathParam(PIN_PARAM);
        PinUpdateRequest request = ctx.bodyAsClass(PinUpdateRequest.class);
        PinResponse response = pinService.updatePin(pinNumber, request);
        ctx.json(response);
    }

    @OpenApi(
        path = CONTEXT_PATH + "/pins/{pinNumber}",
        methods = {HttpMethod.DELETE},
        summary = "Delete a pin by pin number",
        operationId = "deletePin",
        tags = {"Pins"},
        responses = {
            @OpenApiResponse(status = "204", description = "Pin deleted"),
            @OpenApiResponse(status = "404", description = "Pin not found")
        }
    )
    private void deletePin(Context ctx) {
        String pinNumber = ctx.pathParam(PIN_PARAM);
        pinService.deletePin(pinNumber);
        ctx.status(204);
    }

    @OpenApi(
        path = CONTEXT_PATH + "/pins",
        methods = {HttpMethod.GET},
        summary = "Get all pins",
        operationId = "getAllPins",
        tags = {"Pins"},
        responses = {
            @OpenApiResponse(
                status = "200",
                content = {@OpenApiContent(from = PinResponse[].class)}
            )
        }
    )
    private void getAllPins(Context ctx) throws SQLException {
        ctx.json(pinService.getAllPins());
    }
}
