package com.hubbox.demo.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;

record Expose(

    @JsonProperty("access")
    int access,

    @JsonProperty("category")
    String category,

    @JsonProperty("description")
    String description,

    @JsonProperty("label")
    String label,

    @JsonProperty("name")
    String name,

    @JsonProperty("property")
    String property,

    @JsonProperty("type")
    String type,

    @JsonProperty("unit")
    String unit,

    @JsonProperty("value_max")
    int valueMax,

    @JsonProperty("value_min")
    int valueMin,

    @JsonProperty("values")
    ArrayList<String> values,

    @JsonProperty("value_off")
    boolean valueOff,

    @JsonProperty("value_on")
    boolean valueOn
) {
}
