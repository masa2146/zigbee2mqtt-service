package com.hubbox.demo.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceEntity {
    private Long id;
    private Boolean disabled;
    private String friendlyName;
    private String modelId;

}
