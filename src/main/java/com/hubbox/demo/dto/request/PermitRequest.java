package com.hubbox.demo.dto.request;

import net.minidev.json.JSONObject;

public record PermitRequest(
    Boolean value
) {

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("value", value);
        return json;
    }
}
