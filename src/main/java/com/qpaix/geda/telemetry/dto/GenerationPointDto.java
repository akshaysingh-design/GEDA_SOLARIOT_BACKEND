package com.qpaix.geda.telemetry.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class GenerationPointDto {

    private LocalDateTime timestamp;
    private BigDecimal kwh;
}
