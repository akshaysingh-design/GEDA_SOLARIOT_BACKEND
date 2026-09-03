package com.qpaix.geda.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class PlantGenerationDto {

    private Long plantId;
    private String plantName;
    private BigDecimal totalKwhToday;
    private long deviceCount;
}
