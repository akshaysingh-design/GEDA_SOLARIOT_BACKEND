package com.qpaix.geda.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OtpVerifyResponse {

    private String accessToken;
    private UserSummaryDto user;
}
