package com.hubbox.demo.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceCommand {
    private Long id;
    private String modelId;
    private String commandName;
    private String commandTemplate;
    private String description;
}
