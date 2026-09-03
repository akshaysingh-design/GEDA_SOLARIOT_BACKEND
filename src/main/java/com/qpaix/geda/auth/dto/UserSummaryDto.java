package com.qpaix.geda.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserSummaryDto {

    private Long id;
    private String username;
    private String fullName;
    private String role;
    private Long orgUnitId;
}
