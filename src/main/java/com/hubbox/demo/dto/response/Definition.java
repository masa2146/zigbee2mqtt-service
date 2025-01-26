package com.hubbox.demo.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;

public record Definition(

    @JsonProperty("description")
    String description,

    @JsonProperty("exposes")
    ArrayList<Expose> exposes,

    @JsonProperty("model")
    String model,

    @JsonProperty("options")
    ArrayList<Object> options,

    @JsonProperty("supports_ota")
    boolean supportsOta,

    @JsonProperty("vendor")
    String vendor
) {
}
