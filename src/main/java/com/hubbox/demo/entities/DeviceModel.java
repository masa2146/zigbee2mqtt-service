package com.hubbox.demo.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceModel {
    private Long id;
    private Long categoryId;
    private String modelId;
    private String vendor;
    private String description;
}
