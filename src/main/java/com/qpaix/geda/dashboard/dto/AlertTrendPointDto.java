package com.qpaix.geda.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class AlertTrendPointDto {

    private LocalDate date;
    private long highCount;
    private long medCount;
    private long lowCount;
}
