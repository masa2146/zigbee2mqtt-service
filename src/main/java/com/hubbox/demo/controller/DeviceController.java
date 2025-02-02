package com.hubbox.demo.controller;

import static com.hubbox.demo.util.Constants.CONTEXT_PATH;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubbox.demo.dto.request.DeviceCreateRequest;
import com.hubbox.demo.dto.request.DeviceUpdateRequest;
import com.hubbox.demo.dto.response.DeviceResponse;
import com.hubbox.demo.exceptions.DeviceNotFoundException;
import com.hubbox.demo.service.DeviceService;
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
public class DeviceController extends AbstractController {
    private final DeviceService deviceService;

    @Inject
    public DeviceController(DeviceService deviceService) {
        super("devices");
        this.deviceService = deviceService;
    }

    @Override
    public void registerRoutes(Javalin app) {
        app.post(buildPath(), this::createDevice);
        app.put(buildPath("{id}"), this::updateDevice);
        app.get(buildPath(), this::getAllDevices);
        app.get(buildPath("{deviceName}"), this::getDevice);
        app.delete(buildPath("{id}"), this::deleteDevice);
    }

    @OpenApi(
        path = CONTEXT_PATH + "/devices",
        methods = {HttpMethod.POST},
        summary = "Add a new device",
        operationId = "addDevice",
        tags = {"Devices"},
        requestBody = @OpenApiRequestBody(
            content = {@OpenApiContent(from = DeviceCreateRequest.class)}
        ),
        responses = {
            @OpenApiResponse(status = "201", content = {@OpenApiContent(from = DeviceResponse.class)}),
            @OpenApiResponse(status = "400", description = "Invalid request")
        }
    )
    private void createDevice(Context ctx) {
        DeviceCreateRequest device = ctx.bodyAsClass(DeviceCreateRequest.class);
        DeviceResponse response = deviceService.createDevice(device);
        ctx.status(201).json(response);
    }

    @OpenApi(
        path = CONTEXT_PATH + "/devices/{id}",
        methods = {HttpMethod.PUT},
        summary = "Update a device",
        operationId = "updateDevice",
        tags = {"Devices"},
        pathParams = {
            @OpenApiParam(name = "id", type = Long.class, description = "Device ID")
        },
        requestBody = @OpenApiRequestBody(
            content = {@OpenApiContent(from = DeviceUpdateRequest.class)}
        ),
        responses = {
            @OpenApiResponse(status = "201", content = {@OpenApiContent(from = DeviceResponse.class)}),
            @OpenApiResponse(status = "400", description = "Invalid request")
        }
    )
    private void updateDevice(Context ctx) {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        DeviceUpdateRequest device = ctx.bodyAsClass(DeviceUpdateRequest.class);
        DeviceResponse response = deviceService.updateDevice(id, device);
        ctx.status(201).json(response);
    }

    @OpenApi(
        path = CONTEXT_PATH + "/devices",
        methods = {HttpMethod.GET},
        summary = "Get all devices",
        operationId = "getAllDevices",
        tags = {"Devices"},
        responses = {
            @OpenApiResponse(
                status = "200",
                content = {@OpenApiContent(from = DeviceResponse[].class)}
            )
        }
    )
    private void getAllDevices(Context ctx) {
        List<DeviceResponse> devices = deviceService.getAllDevices();
        ctx.json(devices);
    }

    @OpenApi(
        path = CONTEXT_PATH + "/devices/{id}",
        methods = {HttpMethod.DELETE},
        summary = "Delete a device",
        operationId = "deleteDevice",
        tags = {"Devices"},
        pathParams = {
            @OpenApiParam(name = "id", type = Long.class, description = "Device ID")
        },
        responses = {
            @OpenApiResponse(status = "204", description = "Device deleted"),
            @OpenApiResponse(status = "404", description = "Device not found")
        }
    )
    private void deleteDevice(Context ctx) {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        deviceService.deleteDevice(id);
        ctx.status(204);
    }

    @OpenApi(
        path = CONTEXT_PATH + "/devices/{deviceName}",
        methods = {HttpMethod.GET},
        summary = "Get device by name",
        operationId = "getDevice",
        tags = {"Devices"},
        pathParams = {
            @OpenApiParam(name = "deviceName", description = "Name of the device")
        },
        responses = {
            @OpenApiResponse(status = "200", content = {@OpenApiContent(from = DeviceResponse.class)}),
            @OpenApiResponse(status = "404", description = "Device not found")
        }
    )
    private void getDevice(Context ctx) throws DeviceNotFoundException {
        String deviceName = ctx.pathParam("deviceName");
        DeviceResponse device = deviceService.getDeviceById(deviceName);
        ctx.json(device);
    }
}
