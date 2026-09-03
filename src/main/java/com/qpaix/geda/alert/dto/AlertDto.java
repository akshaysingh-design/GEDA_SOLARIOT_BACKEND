package com.qpaix.geda.alert.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
public class AlertDto {

    private Long id;
    private String severity;
    private String message;
    private String deviceCode;
    private String plantName;
    private Instant createdAt;
    private boolean acknowledged;
}
