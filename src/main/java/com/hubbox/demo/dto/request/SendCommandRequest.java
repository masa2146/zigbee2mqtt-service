package com.hubbox.demo.dto.request;

import io.javalin.openapi.OpenApiDescription;

public record SendCommandRequest(
    @OpenApiDescription("The model ID of the device")
    String modelId,

    @OpenApiDescription("The command to send")
    String command
) {

}
