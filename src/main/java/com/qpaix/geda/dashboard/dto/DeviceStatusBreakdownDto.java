package com.qpaix.geda.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DeviceStatusBreakdownDto {

    private long online;
    private long warning;
    private long offline;
}
