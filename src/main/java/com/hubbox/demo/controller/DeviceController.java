package com.hubbox.demo.controller;

import static com.hubbox.demo.util.Constants.CONTEXT_PATH;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubbox.demo.dto.response.DeviceResponse;
import com.hubbox.demo.exceptions.DeviceNotFoundException;
import com.hubbox.demo.service.DeviceService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
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
        app.get(buildPath(), this::getAllDevices);
        app.get(buildPath("{deviceName}"), this::getDevice);
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
