package com.qpaix.geda.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class DashboardKpiDto {

    private long totalDevices;
    private double systemsOnlinePercent;
    private long activeAlertsCount;
    private BigDecimal avgGenerationTodayKwh;
}
