package com.qpaix.geda.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {

    private boolean mfaRequired;
    private String pendingToken;
    private String accessToken;
    private UserSummaryDto user;
    private String devOtpCode;

    public static LoginResponse pendingMfa(String pendingToken, String devOtpCode) {
        LoginResponse response = new LoginResponse();
        response.setMfaRequired(true);
        response.setPendingToken(pendingToken);
        response.setDevOtpCode(devOtpCode);
        return response;
    }

    public static LoginResponse directAccess(String accessToken, UserSummaryDto user) {
        LoginResponse response = new LoginResponse();
        response.setMfaRequired(false);
        response.setAccessToken(accessToken);
        response.setUser(user);
        return response;
    }
}
