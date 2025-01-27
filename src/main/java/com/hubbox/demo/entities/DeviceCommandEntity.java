package com.hubbox.demo.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceCommandEntity {
    private Long id;
    private String modelId;
    private String commandName;
    private String commandTemplate;
    private String description;
}
