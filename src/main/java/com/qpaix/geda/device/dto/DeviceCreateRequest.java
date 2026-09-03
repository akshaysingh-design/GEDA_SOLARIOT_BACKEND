package com.qpaix.geda.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceCreateRequest {

    @NotBlank
    private String deviceCode;

    @NotBlank
    private String name;

    @NotBlank
    private String type;

    @NotNull
    private Long orgUnitId;

    private String status;
}
