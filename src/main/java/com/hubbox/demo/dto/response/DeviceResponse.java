package com.hubbox.demo.dto.response;


import com.fasterxml.jackson.annotation.JsonProperty;

public record DeviceResponse(

    @JsonProperty("id")
    Long id,

    @JsonProperty("disabled")
    Boolean disabled,

    @JsonProperty("friendly_name")
    String friendlyName,

    @JsonProperty("ieee_address")
    String ieeeAddress,

    @JsonProperty("interview_completed")
    Boolean interviewCompleted,

    @JsonProperty("interviewing")
    Boolean interviewing,

    @JsonProperty("network_address")
    Integer networkAddress,

    @JsonProperty("supported")
    Boolean supported,

    @JsonProperty("type")
    String type,

    @JsonProperty("date_code")
    String dateCode,

    @JsonProperty("definition")
    Definition definition,

    @JsonProperty("manufacturer")
    String manufacturer,

    @JsonProperty("model_id")
    String modelId,

    @JsonProperty("power_source")
    String powerSource,

    @JsonProperty("software_build_id")
    String softwareBuildId,

    @JsonProperty("description")
    String description
) {
}
