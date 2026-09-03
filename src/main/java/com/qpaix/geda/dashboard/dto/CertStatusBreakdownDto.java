package com.qpaix.geda.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CertStatusBreakdownDto {

    private long valid;
    private long expiring;
    private long expired;
}
