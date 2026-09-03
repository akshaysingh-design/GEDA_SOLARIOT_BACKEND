package com.qpaix.geda.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtpVerifyRequest {

    @NotBlank
    private String pendingToken;

    @NotBlank
    private String otpCode;
}
