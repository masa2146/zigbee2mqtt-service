package com.hubbox.demo.dto.request;

import net.minidev.json.JSONObject;

public record DeviceRenameRequest(

    String oldName,

    String newName,

    Boolean homeAssistantRename
) {

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        JSONObject data = new JSONObject();
        data.put("from", oldName);
        data.put("to", newName);
        data.put("homeassistant_rename", Boolean.TRUE.equals(homeAssistantRename));
        json.put("data", data);
        return json;
    }
}
