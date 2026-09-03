package com.qpaix.geda.device.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class DeviceDto {

    private Long id;
    private String deviceCode;
    private String name;
    private String type;
    private Long orgUnitId;
    private String orgUnitName;
    private String status;
    private BigDecimal uptimePercent;
    private LocalDate tlsCertValidUntil;
    private String tlsCertStatus;
    private Instant createdAt;
    private Instant lastSeenAt;
}
